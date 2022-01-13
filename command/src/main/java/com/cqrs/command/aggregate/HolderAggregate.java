package com.cqrs.command.aggregate;

import com.cqrs.command.commands.HolderCreationCommand;
import com.cqrs.events.HolderCreationEvent;
import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

@RequiredArgsConstructor
@Aggregate
public class HolderAggregate {
    @AggregateIdentifier
    private String holderID;
    private String holderName;
    private String tel;
    private String address;

    /**
     * Aggregate 에 대한 명령이 발생되었을 때 호출되는 메소드
     * 계정 생성 명령은 곧 HolderAggregate의 생성을 의미
     * @param command
     */
    @CommandHandler
    public HolderAggregate(HolderCreationCommand command) {
        //해당 메소드를 통해서 이벤트를 발행
        AggregateLifecycle.apply(new HolderCreationEvent(command.getHolderID(), command.getHolderName(), command.getTel(), command.getAddress()));
    }

    /**
     * CommandHandler 에서 발생한 이벤트를 적용하는 메소드
     * @param event
     */
    @EventSourcingHandler
    protected void createAccount(HolderCreationEvent event){
        this.holderID = event.getHolderID();
        this.holderName = event.getHolderName();
        this.tel = event.getTel();
        this.address = event.getAddress();
    }
}