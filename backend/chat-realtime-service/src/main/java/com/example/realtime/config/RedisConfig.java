package com.example.realtime.config;

import com.example.realtime.service.RedisMessageSubscriber;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis Configuration
 * Configure RedisTemplate for storing connection metadata
 * Configure Redis Pub/Sub for message distribution
 */
@Slf4j
@Configuration
public class RedisConfig {

    @Value("${chat.realtime.redis.message-channel-prefix:chat:message:}")
    private String messageChannelPrefix;

    @Value("${chat.realtime.redis.typing-channel-prefix:chat:typing:}")
    private String typingChannelPrefix;

    @Value("${chat.realtime.redis.presence-channel-prefix:chat:presence:}")
    private String presenceChannelPrefix;

    /**
     * Plain-JSON ObjectMapper for Redis pub/sub — shared by publisher and subscriber.
     * IMPORTANT: Do NOT call activateDefaultTyping() here.
     * That wraps values as ["ClassName", {...}] which breaks deserialization in the subscriber.
     */
    @Bean(name = "redisObjectMapper")
    public ObjectMapper redisObjectMapper() {
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return om;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory,
                                                        ObjectMapper redisObjectMapper) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Use String serializer for keys
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // Use plain-JSON serializer for values (no type-info array wrapper)
        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer(redisObjectMapper);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();

        log.info("RedisTemplate configured with plain-JSON serialization (no type info)");
        return template;
    }

    /**
     * Redis Message Listener Container
     * Subscribes to Redis Pub/Sub channels
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter messageListenerAdapter) {
        
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        
        // Subscribe to message channels (pattern: chat:message:*)
        container.addMessageListener(messageListenerAdapter, 
                new PatternTopic(messageChannelPrefix + "*"));
        
        // Subscribe to typing channels (pattern: chat:typing:*)
        container.addMessageListener(messageListenerAdapter, 
                new PatternTopic(typingChannelPrefix + "*"));
        
        // Subscribe to presence channels (pattern: chat:presence:*)
        container.addMessageListener(messageListenerAdapter, 
                new PatternTopic(presenceChannelPrefix + "*"));
        
        log.info("Redis Pub/Sub listener configured - patterns: {}*, {}*, {}*", 
                messageChannelPrefix, typingChannelPrefix, presenceChannelPrefix);
        
        return container;
    }

    /**
     * Message Listener Adapter
     */
    @Bean
    public MessageListenerAdapter messageListenerAdapter(RedisMessageSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber, "onMessage");
    }
}
