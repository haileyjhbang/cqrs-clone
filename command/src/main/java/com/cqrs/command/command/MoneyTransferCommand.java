package com.cqrs.command.command;

import com.cqrs.command.dto.TransferDTO;
import com.cqrs.command.transfer.*;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.UUID;
import java.util.function.Function;

@Builder
@ToString
@Getter
public class MoneyTransferCommand {
    private String srcAccountID;
    @TargetAggregateIdentifier
    private String dstAccountID;
    private Long amount;
    private String transferID;
    private BankType bankType;

    public enum BankType{
        JEJU(command -> new TransferCommandFactory(new JejuBankTransferCommand(), new JejuBankCancelTransferCommand(), new JejuBankCompensationCancelCommand())),
        SEOUL(command -> new TransferCommandFactory(new SeoulBankTransferCommand(), new SeoulBankCancelTransferCommand(), new SeoulBankCompensationCancelCommand()))
        ;

        //Function <input, output>
        //apply를 통해서 연산 결과를 받을 수 있음
        private Function<MoneyTransferCommand, TransferCommandFactory> expression;

        BankType(Function<MoneyTransferCommand, TransferCommandFactory> expression){ this.expression = expression;}

        public TransferCommandFactory getCommandFactory(MoneyTransferCommand command){
            TransferCommandFactory factory = this.expression.apply(command);
            factory.create(command.getSrcAccountID(), command.getDstAccountID(), command.getAmount(), command.getTransferID());
            return factory;
        }
    }

    public static MoneyTransferCommand of(TransferDTO dto){
        return MoneyTransferCommand.builder()
                .srcAccountID(dto.getSrcAccountID())
                .dstAccountID(dto.getDstAccountID())
                .amount(dto.getAmount())
                .bankType(dto.getBankType())
                .transferID(UUID.randomUUID().toString())
                .build();
    }
}