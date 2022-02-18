package com.cqrs.command.aggregate;

import com.cqrs.command.command.HolderCreationCommand;
import com.cqrs.event.HolderCreationEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

//////////state stored aggregate 방식
//@AllArgsConstructor
//@NoArgsConstructor
//@Aggregate
//@Slf4j
//@Entity(name = "holder")
//@Table(name = "holder")
//public class HolderAggregate {
//    @AggregateIdentifier
//    @Id
//    @Column(name = "holder_id")
//    @Getter
//    private String holderID;
//    @Column(name = "holder_name")
//    private String holderName;
//    private String tel;
//    private String address;
//    private String company;
//
//    /**
//     * HolderAggregate는 AccountAggregate와 1:N 관계를 맺고 있으므로 양방향 관계 설정 했으며,
//     * HolderAggregate가 삭제될 경우 AccountAggregate도 삭제되도록 orphanRemovel 옵션을 추가했습니다.
//     */
//    @OneToMany(mappedBy = "holder", orphanRemoval = true, fetch = FetchType.EAGER)
//    private List<AccountAggregate> accounts = new ArrayList<>();
//
//    public void registerAccount(AccountAggregate account){
//        if(!this.accounts.contains(account)){
//            this.accounts.add(account);
//        }
//    }
//
//    public void unRegisterAccount(AccountAggregate account){
//        this.accounts.remove(account);
//    }
//
//    @CommandHandler
//    public HolderAggregate(HolderCreationCommand command) {
//        log.debug("handling {}", command);
//
//        this.holderID = command.getHolderID();
//        this.holderName = command.getHolderName();
//        this.tel = command.getTel();
//        this.address = command.getAddress();
//        this.company = command.getCompany();
//
//        AggregateLifecycle.apply(new HolderCreationEvent(command.getHolderID(), command.getHolderName(), command.getTel(), command.getAddress(), command.getCompany()));
//    }
//}
/////////event sourced aggregate 방식

import com.cqrs.command.command.HolderCreationCommand;
import com.cqrs.event.HolderCreationEvent;
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
        log.debug("handling {}", command);
        //해당 메소드를 통해서 이벤트를 발행
        AggregateLifecycle.apply(new HolderCreationEvent(command.getHolderID(), command.getHolderName(), command.getTel(), command.getAddress(), command.getCompany()));
    }

    /**
     * CommandHandler 에서 발생한 이벤트를 적용하는 메소드
     * @param event
     */
    @EventSourcingHandler
    protected void createAccount(HolderCreationEvent event){
        log.debug("applying {}", event);
        this.holderID = event.getHolderID();
        this.holderName = event.getHolderName();
        this.tel = event.getTel();
        this.address = event.getAddress();
    }
}