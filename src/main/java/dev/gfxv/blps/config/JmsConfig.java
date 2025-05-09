package dev.gfxv.blps.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.ConnectionFactory;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;


@EnableJms
@Configuration
public class JmsConfig {

    @Value("${activemq.broker-url}")
    private String brokerUrl;

    @Value("${activemq.username}")
    private String username;

    @Value("${activemq.password}")
    private String password;

    @Value("${jms.listener.concurrency:1-1}")
    private String concurrency;

    @Bean
    public ConnectionFactory connectionFactory() {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
        connectionFactory.setBrokerURL(brokerUrl);
        connectionFactory.setUserName(username);
        connectionFactory.setPassword(password);
        return newCachingConnectionFactory(connectionFactory);
    }

    @Bean
    public JmsTemplate jmsTemplate(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter
    ) {
        JmsTemplate jmsTemplate = new JmsTemplate();
        jmsTemplate.setConnectionFactory(connectionFactory);
        jmsTemplate.setMessageConverter(messageConverter);
        jmsTemplate.setDeliveryPersistent(true);
        return jmsTemplate;
    }

    @Bean
    public MessageConverter messageConverter(ObjectMapper objectMapper) {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(objectMapper);
        converter.setTargetType(org.springframework.jms.support.converter.MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }

    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter
    ) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setConcurrency(concurrency);
        factory.setSessionTransacted(true);
        factory.setErrorHandler(t -> System.err.println("JMS error: " + t.getMessage()));
        return factory;
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    private CachingConnectionFactory newCachingConnectionFactory(ActiveMQConnectionFactory connectionFactory) {
        CachingConnectionFactory cachingFactory = new CachingConnectionFactory(connectionFactory);
        cachingFactory.setSessionCacheSize(10); // Cache up to 10 sessions
        return cachingFactory;
    }
}