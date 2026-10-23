package com.otto.media;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.otto.video.api")
@SpringBootApplication(scanBasePackages = {"com.otto.common", "com.otto.video.api", "com.otto.media"})
public class OttoMediaApplication {

    public static void main(String[] args) {
        SpringApplication.run(OttoMediaApplication.class, args);
    }
}
