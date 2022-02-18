package com.cqrs.command.service;

import com.cqrs.command.aggregate.HolderAggregate;
import com.cqrs.command.command.*;
import com.cqrs.command.dto.*;
import com.cqrs.command.repository.HolderRepository;
import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final CommandGateway commandGateway;
    //private final HolderRepository holders;
    /**
     * send 메소드는 비동기 방식이며, sendAndWait은 동기 방식의 메소드
     * 동기 방식의 sendAndWait 는 default가 응답이 올때까지 대기하며 이는 호출 후 hang 상태가 지속되면 스레드 고갈이 일어날 수 있음
     * 이에 메소드 파라미터에 timeout을 지정하여 실패 처리할 수 있음
     * @param holderDTO
     * @return
     */
    @Override
    public CompletableFuture<String> createHolder(HolderDTO holderDTO) {
        return commandGateway.send(new HolderCreationCommand(UUID.randomUUID().toString()
                ,holderDTO.getHolderName()
                ,holderDTO.getTel()
                ,holderDTO.getAddress()
                ,holderDTO.getCompany())
        );
    }

//    @Override
//    public CompletableFuture<String> createAccount(AccountDTO accountDTO) {
//        HolderAggregate holder = holders.findHolderAggregateByHolderID(accountDTO.getHolderID())
//            .orElseThrow( () -> new IllegalAccessError("계정 ID가 올바르지 않습니다."));
//       // return commandGateway.send(new AccountCreationCommand(UUID.randomUUID().toString(), holder));
//        return commandGateway.send(new AccountCreationCommand(accountDTO.getHolderID(), UUID.randomUUID().toString()));
//    }

    @Override
    public CompletableFuture<String> createAccount(AccountDTO accountDTO) {
        return commandGateway.send(new AccountCreationCommand(UUID.randomUUID().toString(),accountDTO.getHolderID()));
    }

    @Override
    public CompletableFuture<String> depositMoney(DepositDTO transactionDTO) {
        return commandGateway.send(new DepositMoneyCommand(transactionDTO.getAccountID(), transactionDTO.getHolderID(), transactionDTO.getAmount()));
    }

    @Override
    public CompletableFuture<String> withdrawMoney(WithdrawalDTO transactionDTO) {
        return commandGateway.send(new WithdrawMoneyCommand(transactionDTO.getAccountID(), transactionDTO.getHolderID(), transactionDTO.getAmount()));
    }

    //saga
    @Override
    public String transferMoney(TransferDTO transferDTO) {
        return commandGateway.sendAndWait(MoneyTransferCommand.of(transferDTO));
    }
}