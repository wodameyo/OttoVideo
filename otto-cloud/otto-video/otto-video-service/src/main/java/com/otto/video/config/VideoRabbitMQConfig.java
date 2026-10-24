package com.otto.video.config;

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
 * 视频业务 MQ 声明：转码交换机 + 转码结果队列
 */
@Configuration
public class VideoRabbitMQConfig {

    @Bean
    public DirectExchange videoExchange() {
        return new DirectExchange(Constants.MQ_EXCHANGE_VIDEO, true, false);
    }

    @Bean
    public Queue transcodeResultQueue() {
        return QueueBuilder.durable(Constants.MQ_QUEUE_TRANSCODE_RESULT).build();
    }

    @Bean
    public Binding transcodeResultBinding(Queue transcodeResultQueue, DirectExchange videoExchange) {
        return BindingBuilder.bind(transcodeResultQueue).to(videoExchange)
                .with(Constants.MQ_ROUTING_TRANSCODE_RESULT);
    }

    @Bean
    public MessageConverter messageConverter() {
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTrustedPackages("com.otto.video.api", "com.otto.media");
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }
}
