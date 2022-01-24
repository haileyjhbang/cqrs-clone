package com.cqrs.query.service;

import lombok.RequiredArgsConstructor;
import org.axonframework.config.Configuration;
import org.axonframework.eventhandling.TrackingEventProcessor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class QueryServiceImpl implements QueryService{
    private final Configuration configuration;

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