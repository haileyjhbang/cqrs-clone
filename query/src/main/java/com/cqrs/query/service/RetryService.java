package com.cqrs.query.service;

import com.cqrs.query.config.RetryConfig;
import com.cqrs.query.entity.HolderAccountSummary;
import com.cqrs.query.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Slf4j
@Service
//@RequiredArgsConstructor
public class RetryService {
    private final AccountRepository repository;
    private final RetryTemplate retryTemplate = new RetryTemplate();

    public RetryService(AccountRepository repository) {
        this.repository = repository;
        FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
        fixedBackOffPolicy.setBackOffPeriod(2000l); //long type; 딜레이 시간(ms)
        retryTemplate.setBackOffPolicy(fixedBackOffPolicy);

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3); //횟수
        retryTemplate.setRetryPolicy(retryPolicy);
        retryTemplate.registerListener(new RetryConfig.RetryListener());
    }

    @Retryable(
            value = RuntimeException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 3000)
    )
    public HolderAccountSummary getHolderAccountSummary(String holderID) {
        log.debug(">>> getHolder : {} ", holderID);
        return repository.findByHolderId(holderID)
                .orElseThrow(() -> new NoSuchElementException("소유주가 존재하지 않습니다."));
    }

    @Recover
    private HolderAccountSummary recover(RuntimeException e, String holderID) {
        return HolderAccountSummary.builder()
                .name("recovery")
                .build();
    }

    public HolderAccountSummary getHolderAccountSummaryTemplate(String holderID) {
        log.info("retry going on");
        return retryTemplate.execute(context -> {
             return repository.findByHolderId(holderID)
                    .orElseThrow(() -> new NoSuchElementException("소유주가 존재하지 않습니다."));
        });
    }

    public HolderAccountSummary getHolderAccountSummaryTemplateAndRecovery(String holderID){
        log.info("retry going on");
        return retryTemplate.execute(context -> {
            return repository.findByHolderId(holderID)
                    .orElseThrow(() -> new NoSuchElementException("소유주가 존재하지 않습니다."));
        }, recovery -> {
            log.info("recovery start");
            return HolderAccountSummary.builder()
                    .name("recovery")
                    .build();
        });
    }
}
