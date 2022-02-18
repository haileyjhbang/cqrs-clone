package com.cqrs.command.config;

import com.cqrs.command.aggregate.AccountAggregate;
import org.axonframework.commandhandling.SimpleCommandBus;
import org.axonframework.common.caching.Cache;
import org.axonframework.common.caching.WeakReferenceCache;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.eventsourcing.*;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.modelling.command.Repository;
import org.axonframework.springboot.autoconfig.AxonAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/////////event sourced aggregate 방식
@Configuration
@AutoConfigureAfter(AxonAutoConfiguration.class)
public class AxonConfig {
    /**
     * Command 명령 생성과 이를 처리하는 Command Handler를 하나의 App에 모두 구현하였음에도
     * 현 Application에서는 Command 발행 시 Axon Server와의 통신을 수행합니다.
     * 이는 AxonServer와 연결 시 기본적으로 AxonServerCommandBus를 사용하기 때문입니다.
     *
     * 이를 개선하기 위해서는 Command 처리시 AxonServer 연결없이 명령을 처리하도록 변경이 필요합니다.
     * AxonFramework에서는 SimpleCommandBus 클래스를 제공하며, 설정을 통해 CommandBus 인터페이스 교체가 가능합니다.
     *
     * @param transactionManager
     * @return
     */
    //19강에서 saga 구현 중 axon server와 통신을 위해 주석처리
    //@Bean
    SimpleCommandBus commandBus(TransactionManager transactionManager){
        return SimpleCommandBus.builder().transactionManager(transactionManager).build();
    }
///////////snapshot
    @Bean
    public AggregateFactory<AccountAggregate> aggregateFactory(){
        return new GenericAggregateFactory<>(AccountAggregate.class);
    }

    @Bean
    public Snapshotter snapshotter(EventStore eventStore, TransactionManager transactionManager){
        return AggregateSnapshotter
                .builder()
                .eventStore(eventStore)
                .aggregateFactories(aggregateFactory())
                .transactionManager(transactionManager)
                .build();
    }

    @Bean
    public SnapshotTriggerDefinition snapshotTriggerDefinition(EventStore eventStore, TransactionManager transactionManager){
        //이벤트 5개부터 스냅샵
        final int SNAPSHOT_THRESHOLD = 5;
        return new EventCountSnapshotTriggerDefinition(snapshotter(eventStore,transactionManager), SNAPSHOT_THRESHOLD);
    }

    @Bean
    public Repository<AccountAggregate> accountAggregateRepository(EventStore eventStore, SnapshotTriggerDefinition snapshotTriggerDefinition, Cache cache){
        return EventSourcingRepository
                .builder(AccountAggregate.class)
                .eventStore(eventStore)
                .snapshotTriggerDefinition(snapshotTriggerDefinition)
                .cache(cache)
                .build();
    }
///////////snapshot
    @Bean
    public Cache cache(){
        return new WeakReferenceCache();
    }
}