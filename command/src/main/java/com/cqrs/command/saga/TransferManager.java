package com.cqrs.command.saga;

import com.cqrs.command.command.TransferApprovedCommand;
import com.cqrs.command.event.DepositCompletedEvent;
import com.cqrs.command.transfer.TransferCommandFactory;
import com.cqrs.event.transfer.*;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandExecutionException;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.SagaLifecycle;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;

//saga 대상은 noArgsConstructor로 생성되어야 함
@Saga
@Slf4j
public class TransferManager {
    @Autowired
    private transient CommandGateway commandGateway;
    private TransferCommandFactory commandFactory;
    private boolean isExecutingCompensation = false;  //취소 중인가?
    private boolean isAbortingCompensation = false;   //

    //인스턴스의 시작
    //associationProperty 가 인스턴스 별 구분자
    @StartSaga
    @SagaEventHandler(associationProperty = "transferID")
    protected void on(MoneyTransferEvent event) {
        log.debug("Created saga instance");
        log.debug(">>> event : {}", event);
        commandFactory = event.getCommandFactory();
        SagaLifecycle.associateWith("srcAccountID", event.getSrcAccountID());
        try{
            log.info("계좌 이체 시작 : {} ", event);
            commandGateway.sendAndWait(commandFactory.getTransferCommand(), 10, TimeUnit.SECONDS);
        }catch (CommandExecutionException e){
            log.error(">>> failed transfer process. start cancel transaction");
            cancelTransfer(); //취소 시작
        }
    }

    private void cancelTransfer() {
        isExecutingCompensation = true;
        log.info("보상 트랜잭션 요청");
        commandGateway.send(commandFactory.getAbortTransferCommand()); //취소해라
   }

    //취소 성공
    @SagaEventHandler(associationProperty = "srcAccountID")
    protected void on(CompletedCancelTransferEvent event) {
        isExecutingCompensation = false;
        if (!isAbortingCompensation) {
            log.info("계좌 이체 취소 완료 : {} ", event);
            SagaLifecycle.end();
        }
    }

    //이체 성공 시
    @SagaEventHandler(associationProperty = "srcAccountID")
    protected void on(TransferApprovedEvent event) {
        if (!isExecutingCompensation && !isAbortingCompensation) {
            log.info("이체 금액 {} 계좌 반영 요청 : {}", event.getAmount(), event);
            SagaLifecycle.associateWith("accountID", event.getDstAccountID());
            commandGateway.send(TransferApprovedCommand.builder()
                    .accountID(event.getDstAccountID())
                    .amount(event.getAmount())
                    .transferID(event.getTransferID())
                    .build());
        }else{
            log.info("이체 성공 요청 받았지만 현재 진행 중인 취소/복구 요청이 있으으로 진행하지 않습니다.");
        }
    }

    //실패 시
    @SagaEventHandler(associationProperty = "srcAccountID")
    protected void on(TransferDeniedEvent event) {
        log.info("계좌 이체 실패 : {}", event);
        log.info("실패 사유 : {}", event.getDescription());
        if(isExecutingCompensation){ //취소 중이다
            isAbortingCompensation = true;
            log.info("보상 트랜잭션 취소 요청 : {}", event);
            commandGateway.send(commandFactory.getCompensationAbortCommand());
        }else {
            SagaLifecycle.end(); //saga instance 종료
        }
    }

    @SagaEventHandler(associationProperty = "srcAccountID")
    @EndSaga
    protected void on(CompletedCompensationCancelEvent event){
        isAbortingCompensation = false;
        log.info("보상 트랜잭션 취소 완료 : {}",event);
    }

    //트랙젝션 끝나면 saga 인스턴스 종료
    @SagaEventHandler(associationProperty = "accountID")
    @EndSaga
    protected void on(DepositCompletedEvent event){
        log.info("계좌 이체 성공 : {}", event);
    }
}