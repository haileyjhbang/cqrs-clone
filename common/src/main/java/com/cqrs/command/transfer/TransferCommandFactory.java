package com.cqrs.command.transfer;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TransferCommandFactory {
    private final AbstractTransferCommand transferCommand;
    private final AbstractCancelTransferCommand abortTransferCommand;
    private final AbstractCompensationCancelCommand compensationAbortCommand;

    public void create(String srcAccountID, String dstAccountID, Long amount, String transferID){
        transferCommand.create(srcAccountID, dstAccountID, amount, transferID);
        abortTransferCommand.create(srcAccountID, dstAccountID, amount, transferID);
        compensationAbortCommand.create(srcAccountID, dstAccountID, amount, transferID);
    }

    public AbstractTransferCommand getTransferCommand(){
        return this.transferCommand;
    }
    public AbstractCancelTransferCommand getAbortTransferCommand(){
        return this.abortTransferCommand;
    }
    //복구
    public AbstractCompensationCancelCommand getCompensationAbortCommand(){
        return this.compensationAbortCommand;
    }
}