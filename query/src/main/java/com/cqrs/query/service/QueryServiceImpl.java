package com.cqrs.query.service;

import com.cqrs.query.entity.HolderAccountSummary;
import com.cqrs.query.query.AccountQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.config.Configuration;
import org.axonframework.eventhandling.TrackingEventProcessor;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.SubscriptionQueryResult;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Slf4j
@RequiredArgsConstructor
@Service
public class QueryServiceImpl implements QueryService{
    private final Configuration configuration;
    private final QueryGateway queryGateway;

    /**
     * TrackingEventProcessor
     * default
     * Thread 수 : 1개 / 이벤트 하나당 단일 스레드
     * Batch Size : 1
     * 최대 Thread 수 : Segment 개수
     * TokenClaim 주기 : 5000ms
     * -> 개선 @AxonConfig
     */
    @Override
    public void reset() {
        configuration.eventProcessingConfiguration()
                .eventProcessorByProcessingGroup("accounts",
                        TrackingEventProcessor.class)
                .ifPresent(trackingEventProcessor -> {
                    trackingEventProcessor.shutDown();
                    trackingEventProcessor.resetTokens(); // 토큰초기화
                    trackingEventProcessor.start();
                });
    }

    @Override
    public HolderAccountSummary getAccountInfo(String holderId) {
        AccountQuery accountQuery = new AccountQuery(holderId);
        log.debug(">>> queryServiceImpl getAccountInfo handling {}", accountQuery);
        return queryGateway.query(accountQuery, ResponseTypes.instanceOf(HolderAccountSummary.class)).join();
    }

    @Override
    public Flux<HolderAccountSummary> getAccountInfoSubscription(String holderId) {
        AccountQuery accountQuery = new AccountQuery(holderId);
        log.debug(">>> queryServiceImpl getAccountInfoSubscription handling {}", accountQuery);

        SubscriptionQueryResult<HolderAccountSummary, HolderAccountSummary> queryResult = queryGateway.subscriptionQuery(accountQuery,
                ResponseTypes.instanceOf(HolderAccountSummary.class),
                ResponseTypes.instanceOf(HolderAccountSummary.class)
        );

        return Flux.create(emitter -> {
            queryResult.initialResult().subscribe(emitter::next);
            queryResult.updates()
                    .doOnNext(holder -> {
                        log.debug("doOnNext : {}, isCanceled {}", holder, emitter.isCancelled());
                        if (emitter.isCancelled()) {
                            queryResult.close();
                        }
                    })
                    .doOnComplete(emitter::complete)
                    .subscribe(emitter::next);
        });
    }
}