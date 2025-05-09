package dev.gfxv.blps.service.jms;

import dev.gfxv.blps.payload.request.WithdrawRequest;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class WithdrawRequestSender {

    private static final Logger logger = LoggerFactory.getLogger(WithdrawRequestSender.class);
    private static final String QUEUE_NAME = "withdraw-request-queue";

    private final JmsTemplate jmsTemplate;

    public void sendRequest(WithdrawRequest withdrawRequest) {
        try {
            logger.info("Sending withdraw request DTO to queue {}: {}", QUEUE_NAME, withdrawRequest);
            jmsTemplate.convertAndSend(QUEUE_NAME, withdrawRequest);
            logger.debug("Successfully sent withdraw request DTO: {}", withdrawRequest);
        } catch (Exception e) {
            logger.error("Failed to send withdraw request DTO: {}", withdrawRequest, e);
            throw new RuntimeException("Failed to send comment to queue", e);
        }
    }
}
