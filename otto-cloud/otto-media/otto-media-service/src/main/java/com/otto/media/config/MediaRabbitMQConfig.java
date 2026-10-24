package com.otto.media.config;

import com.otto.common.constants.Constants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 媒资 MQ 声明：转码交换机 + 转码请求队列（与 video-service 声明保持一致）
 */
@Configuration
public class MediaRabbitMQConfig {

    @Bean
    public DirectExchange videoExchange() {
        return new DirectExchange(Constants.MQ_EXCHANGE_VIDEO, true, false);
    }

    @Bean
    public Queue transcodeQueue() {
        return QueueBuilder.durable(Constants.MQ_QUEUE_TRANSCODE).build();
    }

    @Bean
    public Binding transcodeBinding(Queue transcodeQueue, DirectExchange videoExchange) {
        return BindingBuilder.bind(transcodeQueue).to(videoExchange).with(Constants.MQ_ROUTING_TRANSCODE);
    }

    @Bean
    public MessageConverter messageConverter() {
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTrustedPackages("com.otto.media", "com.otto.video.api");
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }
}
