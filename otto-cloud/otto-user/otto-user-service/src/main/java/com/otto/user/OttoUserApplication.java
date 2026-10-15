package com.otto.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@MapperScan("com.otto.user.mappers")
@SpringBootApplication(scanBasePackages = {"com.otto.common", "com.otto.user"})
public class OttoUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(OttoUserApplication.class, args);
    }
}
