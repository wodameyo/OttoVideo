package com.otto.media.config;

import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 转码消费独立线程池
 */
@Configuration
public class TranscodeRabbitConfig {

    @Bean("transcodeExecutor")
    public ThreadPoolTaskExecutor transcodeExecutor(TranscodeProperties transcodeProperties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(transcodeProperties.getCoreSize());
        executor.setMaxPoolSize(transcodeProperties.getMaxSize());
        executor.setQueueCapacity(transcodeProperties.getQueueCapacity());
        executor.setThreadNamePrefix("transcode-");
        executor.initialize();
        return executor;
    }

    @Bean("transcodeContainerFactory")
    public SimpleRabbitListenerContainerFactory transcodeContainerFactory(
            ConnectionFactory connectionFactory,
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            @Qualifier("transcodeExecutor") TaskExecutor transcodeExecutor,
            TranscodeProperties transcodeProperties) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setTaskExecutor(transcodeExecutor);
        factory.setConcurrentConsumers(transcodeProperties.getCoreSize());
        factory.setMaxConcurrentConsumers(transcodeProperties.getMaxSize());
        factory.setPrefetchCount(1);
        return factory;
    }
}
