package com.cqrs.command.command;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@ToString
@Getter
@Builder
public class TransferApprovedCommand {
    @TargetAggregateIdentifier
    private String accountID;
    private Long amount;
    private String transferID;
}