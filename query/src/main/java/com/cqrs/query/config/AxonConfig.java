package com.cqrs.query.config;

import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.eventhandling.TrackingEventProcessorConfiguration;
import org.axonframework.eventhandling.async.SequentialPerAggregatePolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AxonConfig {

    @Autowired
    public void configure(EventProcessingConfigurer eventProcessingConfigurer){
        eventProcessingConfigurer.registerTrackingEventProcessor(
          "accounts",
                org.axonframework.config.Configuration::eventStore,
                configuration -> TrackingEventProcessorConfiguration
                        .forParallelProcessing(3)
                        .andBatchSize(100) //default 1
        );

        eventProcessingConfigurer.registerSequencingPolicy(
            "accounts",
                configuration -> SequentialPerAggregatePolicy.instance()
        );
    }
}
