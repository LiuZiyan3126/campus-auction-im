package com.ca;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.ca.module.**.mapper")
@EnableScheduling
@EnableAsync
public class CaApplication {

    public static void main(String[] args) {
        SpringApplication.run(CaApplication.class, args);
    }
}
