package com.mostar.langchain4jtest;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.mostar.langchain4jtest.mapper")
public class Langchain4jTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(Langchain4jTestApplication.class, args);
    }

}
