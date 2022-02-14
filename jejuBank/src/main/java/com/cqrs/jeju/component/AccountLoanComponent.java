package com.cqrs.jeju.component;

import com.cqrs.loan.LoanLimitQuery;
import com.cqrs.loan.LoanLimitResult;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AccountLoanComponent {

    @QueryHandler
    private LoanLimitResult on(LoanLimitQuery query) {
        log.debug(">>> handling {}", query);
        return LoanLimitResult.builder()
                .holderID(query.getHolderID())
                .balance(query.getBalance())
                .bankName("JejuBank")
                .loanLimit(Double.valueOf(query.getBalance() * 1.2).longValue()) // jeju 은행의 대출한도는 일괄적으로 보유 잔고의 120%만 가능하도록 가정
                .build();
    }
}