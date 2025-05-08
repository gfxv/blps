package dev.gfxv.blps;

import com.stripe.exception.StripeException;
import dev.gfxv.blps.entity.Withdrawal;
import dev.gfxv.blps.jca.StripeConnection;
import dev.gfxv.blps.jca.StripeConnectionFactory;
import dev.gfxv.blps.repository.WithdrawalRepository;
import dev.gfxv.blps.service.MonetizationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.AssertionErrors.assertNotNull;
@SpringBootTest
public class StripeIntegrationTest {

    @MockBean
    private MonetizationService monetizationService;

    @MockBean
    private WithdrawalRepository withdrawalRepository;

    @MockBean
    private StripeConnectionFactory stripeConnectionFactory;

    @Test
    void testSuccessfulPayout() throws StripeException {

        StripeConnection mockConnection = mock(StripeConnection.class);
        when(stripeConnectionFactory.getConnection()).thenReturn(mockConnection);
        when(mockConnection.createPayout(any(), anyLong(), any())).thenReturn("po_12345");

        when(withdrawalRepository.findByStripePayoutId("po_12345"))
                .thenReturn(new Withdrawal());

        monetizationService.withdrawEarningsWithStripe("user1", 100.0);

        verify(withdrawalRepository).save(any(Withdrawal.class));
    }

}