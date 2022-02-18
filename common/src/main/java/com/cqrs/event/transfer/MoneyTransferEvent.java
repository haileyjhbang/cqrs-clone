package com.cqrs.event.transfer;

import com.cqrs.command.transfer.TransferCommandFactory;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@ToString
@Getter
public class MoneyTransferEvent {
    private String dstAccountID;
    private String srcAccountID;
    private Long amount;
    private String transferID;
    private TransferCommandFactory commandFactory;
}