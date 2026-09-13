package com.redtourism;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.redtourism.mapper")
public class RedTourismApplication {
    public static void main(String[] args) {
        SpringApplication.run(RedTourismApplication.class, args);
    }
}
