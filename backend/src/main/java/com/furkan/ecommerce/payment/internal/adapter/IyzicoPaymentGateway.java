package com.furkan.ecommerce.payment.internal.adapter;

import com.furkan.ecommerce.auth.api.dto.AuthPaymentProfileView;
import com.furkan.ecommerce.common.exception.BusinessException;
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
    public PaymentResult initCheckout(CheckoutRequest checkout) {
        Options options = options();
        CreateCheckoutFormInitializeRequest request = new CreateCheckoutFormInitializeRequest();
        request.setLocale(Locale.TR.getValue());
        request.setConversationId(checkout.attemptReference());
        request.setPrice(checkout.amount());
        request.setPaidPrice(checkout.amount());
        request.setCurrency(Currency.TRY.name());
        request.setBasketId(String.valueOf(checkout.orderId()));
        request.setPaymentGroup(PaymentGroup.PRODUCT.name());
        request.setCallbackUrl(checkout.callbackUrl());
        request.setBuyer(toBuyer(checkout.userProfile(), checkout.customerIp()));
        Address address = toAddress(checkout.userProfile());
        request.setBillingAddress(address);
        request.setShippingAddress(address);
        request.setBasketItems(toBasketItems(checkout.lines()));

        CheckoutFormInitialize response = CheckoutFormInitialize.create(request, options);
        if (!Status.SUCCESS.getValue().equals(response.getStatus())) {
            return PaymentResult.failure(codeOrDefault(response.getErrorCode(), "IYZICO_CHECKOUT_INIT_FAILED"));
        }
        return PaymentResult.pendingAction(
                "PAYMENT_PENDING_ACTION_REQUIRED",
                response.getPaymentPageUrl(),
                response.getToken(),
                checkout.attemptReference(),
                checkout.expiresAt()
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
                    response.getConversationId(),
                    response.getPaidPrice()
            );
        }

        boolean paid = "SUCCESS".equalsIgnoreCase(response.getPaymentStatus());
        return new VerifyResult(
                paid,
                response.getPaymentId(),
                paid ? null : codeOrDefault(response.getErrorCode(), "PAYMENT_DECLINED"),
                response.getConversationId(),
                response.getPaidPrice()
        );
    }

    private Buyer toBuyer(AuthPaymentProfileView profile, String customerIp) {
        Buyer buyer = new Buyer();
        buyer.setId(String.valueOf(profile.userId()));
        buyer.setName(profile.firstName());
        buyer.setSurname(profile.lastName());
        buyer.setGsmNumber(profile.phoneNumber());
        buyer.setEmail(profile.email());
        buyer.setIdentityNumber(profile.identityNumber());
        buyer.setIp(isBlank(customerIp) ? "127.0.0.1" : customerIp);
        buyer.setRegistrationAddress(profile.address());
        buyer.setCity(profile.city());
        buyer.setCountry(profile.country());
        buyer.setZipCode(profile.zipCode());
        return buyer;
    }

    private Address toAddress(AuthPaymentProfileView profile) {
        Address address = new Address();
        address.setContactName(profile.firstName() + " " + profile.lastName());
        address.setCity(profile.city());
        address.setCountry(profile.country());
        address.setAddress(profile.address());
        address.setZipCode(profile.zipCode());
        return address;
    }

    private List<BasketItem> toBasketItems(List<CheckoutLine> lines) {
        return lines.stream().map(line -> {
            BasketItem item = new BasketItem();
            item.setId(String.valueOf(line.productId()));
            item.setName(line.productName());
            item.setCategory1("Ecommerce");
            item.setItemType(BasketItemType.PHYSICAL.name());
            item.setPrice(line.lineTotal());
            return item;
        }).toList();
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
