package com.cqrs.command;

import org.axonframework.commandhandling.CommandHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CommandApplication {
    public static void main(String[] args){
        SpringApplication.run(CommandApplication.class, args);
    }

    @CommandHandler
    protected void test(Object command){

    }
}