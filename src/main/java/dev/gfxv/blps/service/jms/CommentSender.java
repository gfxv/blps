package dev.gfxv.blps.service.jms;

import dev.gfxv.blps.payload.request.CommentRequest;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class CommentSender {

    private static final Logger logger = LoggerFactory.getLogger(CommentSender.class);
    private static final String QUEUE_NAME = "comment-validation-queue";

    private final JmsTemplate jmsTemplate;

    public void sendComment(CommentRequest commentRequest) {
        try {
            logger.info("Sending comment DTO to queue {}: {}", QUEUE_NAME, commentRequest);
            jmsTemplate.convertAndSend(QUEUE_NAME, commentRequest);
            logger.debug("Successfully sent comment DTO: {}", commentRequest);
        } catch (Exception e) {
            logger.error("Failed to send comment DTO: {}", commentRequest, e);
            throw new RuntimeException("Failed to send comment to queue", e);
        }
    }
}