package com.cqrs.query;

import org.axonframework.eventhandling.EventHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class QueryApplication {
    public static void main(String[] args){
        SpringApplication.run(QueryApplication.class, args);
    }

    @EventHandler
    public void test(){

    }
}
