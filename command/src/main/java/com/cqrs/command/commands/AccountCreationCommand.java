package com.cqrs.command.commands;

import com.cqrs.command.aggregate.HolderAggregate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

//////////state stored aggregate 방식
@AllArgsConstructor
@ToString
@Getter
public class AccountCreationCommand {
    @TargetAggregateIdentifier
    private String accountID;
    private HolderAggregate holder;
}
/////////event sourced aggregate 방식

//@AllArgsConstructor
//@ToString
//@Getter
//public class AccountCreationCommand {
//    @TargetAggregateIdentifier
//    private String holderID;
//    private String accountID;
//}