package com.cqrs.command;

import org.axonframework.commandhandling.CommandHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CommandApplication {
    public static void main(String[] args){
        SpringApplication.run(CommandApplication.class, args);
    }

    /**
     * Application 기동시 AxonServer와 연결을 시도
     * 연결이 완료되면, 해당 App은 자신이 처리가능한 Command Handler 정보를 Server에 등록
     * @param command
     */
    //@CommandHandler
    protected void test(Object command){

    }
}