package com.diwgroup.downstreamservice;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;



@SpringBootApplication
public class DownstreamServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DownstreamServiceApplication.class, args);
    }

}
