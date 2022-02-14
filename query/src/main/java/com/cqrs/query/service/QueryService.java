package com.cqrs.query.service;

import com.cqrs.loan.LoanLimitResult;
import com.cqrs.query.entity.HolderAccountSummary;
import reactor.core.publisher.Flux;

import java.util.List;

public interface QueryService {
    void reset();
    HolderAccountSummary getAccountInfo(String holderId);
    //subscription
    Flux<HolderAccountSummary> getAccountInfoSubscription(String holderId);
    //scatter
    List<LoanLimitResult> getAccountInfoScatterGather(String holderId);
}
