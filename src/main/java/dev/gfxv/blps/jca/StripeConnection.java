package dev.gfxv.blps.jca;

import com.stripe.exception.StripeException;

public interface StripeConnection extends AutoCloseable {
    String createPayout(String currency, long amount, String destination) throws StripeException;

    void addTestFunds(String accountId) throws StripeException;

    String createPayment(String accountId) throws StripeException;

    void addTestExternalAccount(String stripeAccountId) throws StripeException;

    void close();
}
