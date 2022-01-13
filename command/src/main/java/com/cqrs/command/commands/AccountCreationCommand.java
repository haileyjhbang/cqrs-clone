package com.cqrs.command.commands;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@AllArgsConstructor
@ToString
@Getter
public class AccountCreationCommand {
    @TargetAggregateIdentifier
    private String holderID;
    private String accountID;
}