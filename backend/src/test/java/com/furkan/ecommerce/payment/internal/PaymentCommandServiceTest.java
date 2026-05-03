package com.furkan.ecommerce.payment.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.furkan.ecommerce.auth.api.AuthReadApi;
import com.furkan.ecommerce.auth.api.dto.AuthPaymentProfileView;
import com.furkan.ecommerce.common.domain.BaseEntity;
import com.furkan.ecommerce.common.outbox.DomainEvent;
import com.furkan.ecommerce.common.outbox.OutboxRecorder;
import com.furkan.ecommerce.common.outbox.ProcessedEvent;
import com.furkan.ecommerce.common.outbox.ProcessedEventRepository;
import com.furkan.ecommerce.order.api.OrderReadApi;
import com.furkan.ecommerce.order.api.dto.OrderInventoryView;
import com.furkan.ecommerce.order.api.dto.OrderPaymentView;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

class PaymentCommandServiceTest {
    private FakeGateway gateway;
    private FakeAuthReadApi authReadApi;
    private FakeOrderReadApi orderReadApi;
    private FakePaymentRepository paymentRepository;
    private FakePaymentAttemptRepository paymentAttemptRepository;
    private FakeProcessedEventRepository processedEventRepository;
    private CapturingOutboxRecorder outboxRecorder;
    private PaymentCommandService service;

    @BeforeEach
    void setUp() {
        gateway = new FakeGateway();
        authReadApi = new FakeAuthReadApi();
        orderReadApi = new FakeOrderReadApi();
        paymentRepository = new FakePaymentRepository();
        paymentAttemptRepository = new FakePaymentAttemptRepository();
        processedEventRepository = new FakeProcessedEventRepository();
        outboxRecorder = new CapturingOutboxRecorder();
        var callbackProperties = new PaymentCallbackProperties(
                "http://localhost:8080/api/v1/payments/callback",
                "http://localhost:5173/payment/result",
                "iyzico",
                Duration.ofMinutes(30),
                new PaymentCallbackProperties.IyzicoProperties("api-key", "secret-key", "https://sandbox-api.iyzipay.com")
        );
        service = new PaymentCommandService(
                gateway,
                authReadApi,
                orderReadApi,
                paymentRepository.proxy(),
                paymentAttemptRepository.proxy(),
                processedEventRepository.proxy(),
                outboxRecorder,
                callbackProperties,
                new TransactionTemplate(transactionManager()),
                Mappers.getMapper(PaymentMapper.class)
        );
    }

    @Test
    void should_reject_payment_init_for_another_users_order() {
        orderReadApi.paymentView = Optional.of(orderView(10L, 99L, BigDecimal.TEN));

        assertThatThrownBy(() -> service.init(1L, 10L, "127.0.0.1"))
                .isInstanceOf(AccessDeniedException.class);

        assertThat(gateway.initCalls).isZero();
    }

    @Test
    void should_use_order_total_amount_when_initializing_checkout() {
        orderReadApi.paymentView = Optional.of(orderView(10L, 1L, BigDecimal.valueOf(42)));
        authReadApi.profile = Optional.of(profile());
        gateway.initResult = PaymentGateway.PaymentResult.pendingAction(
                "PAYMENT_PENDING_ACTION_REQUIRED", "url", "token", "order-10-test", Instant.now().plusSeconds(300)
        );

        PaymentGateway.PaymentResult result = service.init(1L, 10L, "10.0.0.1");

        assertThat(result.checkoutUrl()).isEqualTo("url");
        assertThat(gateway.lastRequest.amount()).isEqualByComparingTo(BigDecimal.valueOf(42));
        assertThat(gateway.lastRequest.customerIp()).isEqualTo("10.0.0.1");
    }

    @Test
    void should_not_call_gateway_when_active_attempt_already_exists() {
        Payment payment = paymentRepository.store(Payment.initiate(10L, BigDecimal.TEN, "iyzico"));
        PaymentAttempt attempt = paymentAttemptRepository.store(PaymentAttempt.request(
                payment, 10L, "order-10-existing", BigDecimal.TEN, Instant.now().plusSeconds(300)
        ));
        attempt.markActionRequired("url", "token");
        payment.markActionRequired("url", "token", "order-10-existing");
        orderReadApi.paymentView = Optional.of(orderView(10L, 1L, BigDecimal.TEN));

        PaymentGateway.PaymentResult result = service.init(1L, 10L, "127.0.0.1");

        assertThat(result.checkoutToken()).isEqualTo("token");
        assertThat(gateway.initCalls).isZero();
    }

    @Test
    void should_suppress_business_effect_when_callback_token_is_replayed() {
        gateway.verifyResult = new PaymentGateway.VerifyResult(true, "tx-1", null, "order-10-existing", BigDecimal.TEN);
        orderReadApi.paymentView = Optional.of(orderView(10L, 1L, BigDecimal.TEN));
        processedEventRepository.throwDuplicate = true;

        service.handleCallback("token");

        assertThat(outboxRecorder.events).isEmpty();
        assertThat(paymentRepository.saveCalls).isZero();
    }

    @Test
    void should_record_success_event_after_verified_callback() {
        Payment payment = paymentRepository.store(Payment.initiate(10L, BigDecimal.TEN, "iyzico"));
        PaymentAttempt attempt = paymentAttemptRepository.store(PaymentAttempt.request(
                payment, 10L, "order-10-existing", BigDecimal.TEN, Instant.now().plusSeconds(300)
        ));
        attempt.markActionRequired("url", "token");
        payment.markActionRequired("url", "token", "order-10-existing");
        gateway.verifyResult = new PaymentGateway.VerifyResult(true, "tx-1", null, "order-10-existing", BigDecimal.TEN);
        orderReadApi.paymentView = Optional.of(orderView(10L, 1L, BigDecimal.TEN));

        service.handleCallback("token");

        assertThat(outboxRecorder.events).hasSize(1);
        assertThat(outboxRecorder.events.getFirst().getClass().getSimpleName()).isEqualTo("PaymentSucceededEvent");
    }

    private OrderPaymentView orderView(Long orderId, Long userId, BigDecimal totalAmount) {
        return new OrderPaymentView(
                orderId,
                userId,
                "PENDING",
                totalAmount,
                Instant.now().plusSeconds(1800),
                List.of(new OrderPaymentView.OrderPaymentLineView(1L, "Test Ürün", null, totalAmount, 1))
        );
    }

    private AuthPaymentProfileView profile() {
        return new AuthPaymentProfileView(
                1L,
                "user@test.com",
                "Furkan",
                "Boncuk",
                "+905551112233",
                "12345678901",
                "Test Mahallesi Test Sokak",
                "Istanbul",
                "Turkey",
                "34000"
        );
    }

    private PlatformTransactionManager transactionManager() {
        return new PlatformTransactionManager() {
            @Override
            public TransactionStatus getTransaction(TransactionDefinition definition) {
                return new SimpleTransactionStatus();
            }

            @Override
            public void commit(TransactionStatus status) {
            }

            @Override
            public void rollback(TransactionStatus status) {
            }
        };
    }

    private static class FakeGateway implements PaymentGateway {
        int initCalls;
        CheckoutRequest lastRequest;
        PaymentResult initResult = PaymentResult.pendingAction("PAYMENT_PENDING_ACTION_REQUIRED", "url", "token", "order-10-test", Instant.now().plusSeconds(300));
        VerifyResult verifyResult = new VerifyResult(true, "tx-1", null, "order-10-test", BigDecimal.TEN);

        @Override
        public PaymentResult initCheckout(CheckoutRequest request) {
            initCalls++;
            lastRequest = request;
            return initResult;
        }

        @Override
        public VerifyResult verifyCheckout(String checkoutToken) {
            return verifyResult;
        }
    }

    private static class FakeAuthReadApi implements AuthReadApi {
        Optional<AuthPaymentProfileView> profile = Optional.empty();

        @Override
        public Optional<AuthPaymentProfileView> findPaymentProfileById(Long userId) {
            return profile;
        }
    }

    private static class FakeOrderReadApi implements OrderReadApi {
        Optional<OrderPaymentView> paymentView = Optional.empty();

        @Override
        public Optional<OrderInventoryView> findInventoryViewById(Long orderId) {
            return Optional.empty();
        }

        @Override
        public Optional<OrderPaymentView> findPaymentViewById(Long orderId) {
            return paymentView;
        }
    }

    private static class FakePaymentRepository {
        Payment payment;
        int saveCalls;

        Payment store(Payment value) {
            payment = value;
            setId(payment, 1L);
            return payment;
        }

        PaymentRepository proxy() {
            return (PaymentRepository) Proxy.newProxyInstance(
                    PaymentRepository.class.getClassLoader(),
                    new Class<?>[]{PaymentRepository.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "findByOrderId" -> Optional.ofNullable(payment);
                        case "save", "saveAndFlush" -> {
                            saveCalls++;
                            payment = (Payment) args[0];
                            setId(payment, 1L);
                            yield payment;
                        }
                        default -> throw new UnsupportedOperationException(method.getName());
                    }
            );
        }
    }

    private static class FakePaymentAttemptRepository {
        PaymentAttempt attempt;

        PaymentAttempt store(PaymentAttempt value) {
            attempt = value;
            setId(attempt, 1L);
            return attempt;
        }

        PaymentAttemptRepository proxy() {
            return (PaymentAttemptRepository) Proxy.newProxyInstance(
                    PaymentAttemptRepository.class.getClassLoader(),
                    new Class<?>[]{PaymentAttemptRepository.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "save", "saveAndFlush" -> store((PaymentAttempt) args[0]);
                        case "findById" -> Optional.ofNullable(attempt);
                        case "findFirstByPaymentOrderByIdDesc" -> Optional.ofNullable(attempt);
                        case "findByAttemptReference" -> Optional.ofNullable(attempt)
                                .filter(value -> value.getAttemptReference().equals(args[0]));
                        case "findByCheckoutToken" -> Optional.ofNullable(attempt)
                                .filter(value -> args[0] != null && args[0].equals(value.getCheckoutToken()));
                        case "findFirstByPaymentAndStatusInAndExpiresAtAfterOrderByIdDesc" -> Optional.ofNullable(attempt)
                                .filter(value -> value.isActive((Instant) args[2]));
                        default -> throw new UnsupportedOperationException(method.getName());
                    }
            );
        }
    }

    private static class FakeProcessedEventRepository {
        boolean throwDuplicate;

        ProcessedEventRepository proxy() {
            return (ProcessedEventRepository) Proxy.newProxyInstance(
                    ProcessedEventRepository.class.getClassLoader(),
                    new Class<?>[]{ProcessedEventRepository.class},
                    (proxy, method, args) -> {
                        if ("saveAndFlush".equals(method.getName())) {
                            if (throwDuplicate) {
                                throw new DataIntegrityViolationException("duplicate");
                            }
                            return (ProcessedEvent) args[0];
                        }
                        throw new UnsupportedOperationException(method.getName());
                    }
            );
        }
    }

    private static class CapturingOutboxRecorder extends OutboxRecorder {
        private final List<DomainEvent> events = new ArrayList<>();

        CapturingOutboxRecorder() {
            super(null, null);
        }

        @Override
        public void record(DomainEvent event) {
            events.add(event);
        }
    }

    private static void setId(BaseEntity entity, Long id) {
        try {
            Field field = BaseEntity.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
