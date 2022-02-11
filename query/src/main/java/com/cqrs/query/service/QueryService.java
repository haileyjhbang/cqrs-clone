package com.cqrs.query.service;

import com.cqrs.query.entity.HolderAccountSummary;
import reactor.core.publisher.Flux;

public interface QueryService {
    void reset();
    HolderAccountSummary getAccountInfo(String holderId);
    //subscription
    Flux<HolderAccountSummary> getAccountInfoSubscription(String holderId);
}
