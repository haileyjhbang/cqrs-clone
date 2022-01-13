package com.cqrs.command.aggregate;

import com.cqrs.command.commands.AccountCreationCommand;
import com.cqrs.command.commands.DepositMoneyCommand;
import com.cqrs.command.commands.WithdrawMoneyCommand;
import com.cqrs.events.AccountCreationEvent;
import com.cqrs.events.DepositMoneyEvent;
import com.cqrs.events.WithdrawMoneyEvent;
import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

@RequiredArgsConstructor
@Aggregate
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
        AggregateLifecycle.apply(new AccountCreationEvent(command.getHolderID(),command.getAccountID()));
    }

    /**
     * CommandHandler 에서 발생한 이벤트를 적용하는 메소드
     * EventStore에 적재된 모든 Event는 재생해야할 대상이기 때문에 EventSourcingHandler에서는 Replay만 수행
     * @param event
     */
    @EventSourcingHandler
    protected void createAccount(AccountCreationEvent event){
        this.accountID = event.getAccountID();
        this.holderID = event.getHolderID();
        this.balance = 0L;
    }
///////
    @CommandHandler
    protected void depositMoney(DepositMoneyCommand command){
        if(command.getAmount() <= 0) throw new IllegalStateException("amount >= 0");
        AggregateLifecycle.apply(new DepositMoneyEvent(command.getHolderID(), command.getAccountID(), command.getAmount()));
    }
    @EventSourcingHandler
    protected void depositMoney(DepositMoneyEvent event){
        this.balance += event.getAmount();
    }
///////
    @CommandHandler
    protected void withdrawMoney(WithdrawMoneyCommand command){
        if(this.balance - command.getAmount() < 0) throw new IllegalStateException("잔고가 부족합니다.");
        else if(command.getAmount() <= 0 ) throw new IllegalStateException("amount >= 0");
        AggregateLifecycle.apply(new WithdrawMoneyEvent(command.getHolderID(), command.getAccountID(), command.getAmount()));
    }
    @EventSourcingHandler
    protected void withdrawMoney(WithdrawMoneyEvent event){
        this.balance -= event.getAmount();
    }
}