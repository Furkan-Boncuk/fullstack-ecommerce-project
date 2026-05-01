package com.furkan.ecommerce.payment.internal.adapter;

import com.furkan.ecommerce.common.exception.BusinessException;
import com.furkan.ecommerce.auth.api.dto.AuthPaymentProfileView;
import com.furkan.ecommerce.payment.internal.PaymentCallbackProperties;
import com.furkan.ecommerce.payment.internal.PaymentGateway;
import com.iyzipay.Options;
import com.iyzipay.model.Address;
import com.iyzipay.model.BasketItem;
import com.iyzipay.model.BasketItemType;
import com.iyzipay.model.Buyer;
import com.iyzipay.model.CheckoutForm;
import com.iyzipay.model.CheckoutFormInitialize;
import com.iyzipay.model.Currency;
import com.iyzipay.model.Locale;
import com.iyzipay.model.PaymentGroup;
import com.iyzipay.model.Status;
import com.iyzipay.request.CreateCheckoutFormInitializeRequest;
import com.iyzipay.request.RetrieveCheckoutFormRequest;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
@RequiredArgsConstructor
public class IyzicoPaymentGateway implements PaymentGateway {
    private final PaymentCallbackProperties properties;

    @Override
    public PaymentResult initCheckout(Long orderId, BigDecimal amount, AuthPaymentProfileView userProfile) {
        Options options = options();
        CreateCheckoutFormInitializeRequest request = new CreateCheckoutFormInitializeRequest();
        request.setLocale(Locale.TR.getValue());
        request.setConversationId(String.valueOf(orderId));
        request.setPrice(amount);
        request.setPaidPrice(amount);
        request.setCurrency(Currency.TRY.name());
        request.setBasketId(String.valueOf(orderId));
        request.setPaymentGroup(PaymentGroup.PRODUCT.name());
        request.setCallbackUrl(properties.callbackUrl());

        Buyer buyer = new Buyer();
        buyer.setId(String.valueOf(userProfile.userId()));
        buyer.setName(userProfile.firstName());
        buyer.setSurname(userProfile.lastName());
        buyer.setGsmNumber(userProfile.phoneNumber());
        buyer.setEmail(userProfile.email());
        buyer.setIdentityNumber(userProfile.identityNumber());
        buyer.setIp("127.0.0.1");
        buyer.setRegistrationAddress(userProfile.address());
        buyer.setCity(userProfile.city());
        buyer.setCountry(userProfile.country());
        buyer.setZipCode(userProfile.zipCode());
        request.setBuyer(buyer);

        Address address = new Address();
        address.setContactName(userProfile.firstName() + " " + userProfile.lastName());
        address.setCity(userProfile.city());
        address.setCountry(userProfile.country());
        address.setAddress(userProfile.address());
        address.setZipCode(userProfile.zipCode());
        request.setBillingAddress(address);
        request.setShippingAddress(address);

        BasketItem item = new BasketItem();
        item.setId("order-" + orderId);
        item.setName("Order #" + orderId);
        item.setCategory1("Ecommerce");
        item.setItemType(BasketItemType.PHYSICAL.name());
        item.setPrice(amount);
        request.setBasketItems(List.of(item));

        CheckoutFormInitialize response = CheckoutFormInitialize.create(request, options);
        if (!Status.SUCCESS.getValue().equals(response.getStatus())) {
            return PaymentResult.failure(codeOrDefault(response.getErrorCode(), "IYZICO_CHECKOUT_INIT_FAILED"));
        }
        return PaymentResult.pendingAction(
                "PAYMENT_PENDING_ACTION_REQUIRED",
                response.getPaymentPageUrl(),
                response.getToken(),
                String.valueOf(orderId)
        );
    }

    @Override
    public VerifyResult verifyCheckout(String checkoutToken) {
        RetrieveCheckoutFormRequest request = new RetrieveCheckoutFormRequest();
        request.setLocale(Locale.TR.getValue());
        request.setToken(checkoutToken);
        CheckoutForm response = CheckoutForm.retrieve(request, options());

        if (!Status.SUCCESS.getValue().equals(response.getStatus())) {
            return new VerifyResult(
                    false,
                    response.getPaymentId(),
                    codeOrDefault(response.getErrorCode(), "IYZICO_CHECKOUT_VERIFY_FAILED"),
                    response.getConversationId()
            );
        }

        boolean paid = "SUCCESS".equalsIgnoreCase(response.getPaymentStatus());
        return new VerifyResult(
                paid,
                response.getPaymentId(),
                paid ? null : codeOrDefault(response.getErrorCode(), "PAYMENT_DECLINED"),
                response.getConversationId()
        );
    }

    private Options options() {
        if (isBlank(properties.iyzico().apiKey()) || isBlank(properties.iyzico().secretKey()) || isBlank(properties.iyzico().baseUrl())) {
            throw new BusinessException("IYZICO_CONFIG_MISSING", "Iyzico configuration is missing");
        }
        Options options = new Options();
        options.setApiKey(properties.iyzico().apiKey());
        options.setSecretKey(properties.iyzico().secretKey());
        options.setBaseUrl(properties.iyzico().baseUrl());
        return options;
    }

    private String codeOrDefault(String errorCode, String fallbackCode) {
        return isBlank(errorCode) ? fallbackCode : errorCode;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
