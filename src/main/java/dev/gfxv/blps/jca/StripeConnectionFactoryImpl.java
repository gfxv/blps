package dev.gfxv.blps.jca;


import com.stripe.exception.StripeException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StripeConnectionFactoryImpl implements StripeConnectionFactory {

    @Value("${stripe.secret.key}")
    private String secretKey;

    @Override
    public StripeConnection getConnection() throws StripeException {
        return new StripeConnectionImpl(secretKey);
    }
}

