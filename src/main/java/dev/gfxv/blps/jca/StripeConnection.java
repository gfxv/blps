package dev.gfxv.blps.jca;

import com.stripe.exception.StripeException;

public interface StripeConnection extends AutoCloseable {
    String createPayout(String currency, long amount, String destination) throws StripeException;
    void close();
}
