package com.cqrs.command.aggregate;

import com.cqrs.command.commands.AccountCreationCommand;
import com.cqrs.command.commands.DepositMoneyCommand;
import com.cqrs.command.commands.WithdrawMoneyCommand;
import com.cqrs.events.AccountCreationEvent;
import com.cqrs.events.DepositMoneyEvent;
import com.cqrs.events.WithdrawMoneyEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

@RequiredArgsConstructor
@Aggregate
@Slf4j
public class AccountAggregate {
    @AggregateIdentifier
    private String accountID;
    private String holderID;
    private Long balance;

    /**
     * Aggregate 에 대한 명령이 발생되었을 때 호출되는 메소드
     * 예외 처리 및 유효성 검증을 CommandHandler 에서 한 후 검증된 Event만을 발행
     * @param command
     */
    @CommandHandler
    public AccountAggregate(AccountCreationCommand command) {
        System.out.println("step2 call command handler");
        log.debug(">> handling {}", command);
        AggregateLifecycle.apply(new AccountCreationEvent(command.getHolderID(),command.getAccountID()));
    }

    /**
     * CommandHandler 에서 발생한 이벤트를 적용하는 메소드
     * EventStore에 적재된 모든 Event는 재생해야할 대상이기 때문에 EventSourcingHandler에서는 Replay만 수행
     * @param event
     */
    @EventSourcingHandler
    protected void createAccount(AccountCreationEvent event){
        System.out.println("step3 call event source handler");
        log.debug(">> applying {}", event);
        this.accountID = event.getAccountID();
        this.holderID = event.getHolderID();
        this.balance = 0L;
    }
///////
    @CommandHandler
    protected void depositMoney(DepositMoneyCommand command){
        log.debug("handling {}", command);
        if(command.getAmount() <= 0) throw new IllegalStateException("amount >= 0");
        AggregateLifecycle.apply(new DepositMoneyEvent(command.getHolderID(), command.getAccountID(), command.getAmount()));
    }
    @EventSourcingHandler
    protected void depositMoney(DepositMoneyEvent event){
        log.debug("applying {}", event);
        this.balance += event.getAmount();
        log.debug("balance {}", this.balance);
    }
///////
    @CommandHandler
    protected void withdrawMoney(WithdrawMoneyCommand command){
        log.debug("handling {}", command);
        if(this.balance - command.getAmount() < 0) throw new IllegalStateException("잔고가 부족합니다.");
        else if(command.getAmount() <= 0 ) throw new IllegalStateException("amount >= 0");
        AggregateLifecycle.apply(new WithdrawMoneyEvent(command.getHolderID(), command.getAccountID(), command.getAmount()));
    }
    @EventSourcingHandler
    protected void withdrawMoney(WithdrawMoneyEvent event){
        log.debug("applying {}", event);
        this.balance -= event.getAmount();
        log.debug("balance {}", this.balance);
    }
}