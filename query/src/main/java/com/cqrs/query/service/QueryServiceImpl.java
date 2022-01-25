package com.cqrs.query.service;

import lombok.RequiredArgsConstructor;
import org.axonframework.config.Configuration;
import org.axonframework.eventhandling.TrackingEventProcessor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class QueryServiceImpl implements QueryService{
    private final Configuration configuration;

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
}