package com.otto.video;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableRabbit
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.otto.user.api")
@MapperScan("com.otto.video.mappers")
@SpringBootApplication(scanBasePackages = {"com.otto.common", "com.otto.video"})
public class OttoVideoApplication {

    public static void main(String[] args) {
        SpringApplication.run(OttoVideoApplication.class, args);
    }
}
