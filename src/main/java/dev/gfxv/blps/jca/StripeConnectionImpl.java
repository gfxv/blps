package dev.gfxv.blps.jca;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Payout;
import com.stripe.net.RequestOptions;

import java.util.HashMap;
import java.util.Map;

public class StripeConnectionImpl implements StripeConnection {

    public StripeConnectionImpl(String apiKey) {
        Stripe.apiKey = apiKey;
    }

    @Override
    public String createPayout(String currency, long amount, String destination) throws StripeException {
        Map<String, Object> params = new HashMap<>();
        params.put("amount", amount);
        params.put("currency", currency);

        RequestOptions requestOptions = RequestOptions.builder()
                .setStripeAccount(destination)
                .build();

        Payout payout = Payout.create(params, requestOptions);
        return payout.getId();
    }


    @Override
    public void close() {
    }
}
