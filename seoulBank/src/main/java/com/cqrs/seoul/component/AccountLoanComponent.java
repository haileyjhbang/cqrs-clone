package com.cqrs.seoul.component;

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
                .bankName("SeoulBank")
                .loanLimit(Double.valueOf(query.getBalance() * 1.5).longValue())
                .build();
    }
}