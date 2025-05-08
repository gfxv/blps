package dev.gfxv.blps.jca;

import com.stripe.exception.StripeException;

public interface StripeConnectionFactory {
    StripeConnection getConnection() throws StripeException;
}

