package dev.gfxv.blps.jca;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.net.RequestOptions;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StripeConnectionImpl implements StripeConnection {

    public StripeConnectionImpl(String apiKey) {
        Stripe.apiKey = apiKey;
    }

    @Override
    public String createPayment(String accountId) throws StripeException {
        Map<String, Object> paymentParams = new HashMap<>();
        paymentParams.put("amount", 500);
        paymentParams.put("currency", "usd");
        paymentParams.put("payment_method_types", List.of("card"));
        paymentParams.put("payment_method", "pm_card_visa");
        paymentParams.put("confirm", true);

        Map<String, Object> transferData = new HashMap<>();
        transferData.put("destination", accountId);
        paymentParams.put("transfer_data", transferData);

        PaymentIntent intent = PaymentIntent.create(paymentParams);
        intent = PaymentIntent.retrieve(intent.getId());

        if (!"succeeded".equals(intent.getStatus())) {
            throw new IllegalStateException("Payment not completed");
        }
        return intent.getId();
    }

    @Override
    public String createPayout(String currency, long amount, String stripeAccountId) throws StripeException {
        Map<String, Object> params = new HashMap<>();
        params.put("amount", amount);
        params.put("currency", currency);

        RequestOptions requestOptions = RequestOptions.builder()
                .setStripeAccount(stripeAccountId)
                .build();

        Balance balance = Balance.retrieve(RequestOptions.builder()
                .setStripeAccount(stripeAccountId)
                .build());

        System.out.println(balance.getAvailable());

        Payout payout = Payout.create(params, requestOptions);
        System.out.println("Payout created: " + payout.getId());

        return payout.getId();
    }


    @Override
    public void addTestExternalAccount(String stripeAccountId) throws StripeException {
        Account account = Account.retrieve(stripeAccountId);

        Map<String, Object> params = new HashMap<>();
        params.put("external_account", "btok_us_verified");
        account.getExternalAccounts().create(params);
    }

    public void addTestFunds(String accountId) throws StripeException {
        Map<String, Object> params = new HashMap<>();
        params.put("amount", 1000); // 10 USD в центах
        params.put("currency", "usd");
        params.put("destination", accountId);

        RequestOptions options = RequestOptions.builder()
                .setStripeAccount(accountId)
                .build();

        Transfer.create(params, options);
    }


    @Override
    public void close() {

     }
}
