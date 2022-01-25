package com.cqrs.query.projection;

import com.cqrs.events.AccountCreationEvent;
import com.cqrs.events.DepositMoneyEvent;
import com.cqrs.events.HolderCreationEvent;
import com.cqrs.events.WithdrawMoneyEvent;
import com.cqrs.query.entity.HolderAccountSummary;
import com.cqrs.query.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.AllowReplay;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.ResetHandler;
import org.axonframework.eventhandling.Timestamp;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.NoSuchElementException;

@EnableRetry
@Component
@RequiredArgsConstructor
@Slf4j
@ProcessingGroup("accounts")
public class HolderAccountProjection {
    private final AccountRepository repository;

    /**
     * EventHandler 메소드 파라미터에는 @Timestamp 외 @SequenceNumber, ReplayStatus 등이 추가로 전달될 수 있으며
     * 자세한 내용은 Axon 공식 문서를 참고 바랍니다.
     *
     * @param event
     * @param instant
     */
    @EventHandler
    @AllowReplay
    protected void on(HolderCreationEvent event, @Timestamp Instant instant) {
        log.debug(">>> projecting {} , timestamp : {}", event, instant.toString());
        HolderAccountSummary accountSummary = HolderAccountSummary.builder()
                .holderId(event.getHolderID())
                .name(event.getHolderName())
                .address(event.getAddress())
                .tel(event.getTel())
                .totalBalance(0L)
                .accountCnt(0L)
                .build();
        repository.save(accountSummary);
    }

    @Retryable(value = {NoSuchElementException.class}, maxAttempts = 5, backoff = @Backoff(delay = 1000))
    @EventHandler
    @AllowReplay
    protected void on(AccountCreationEvent event, @Timestamp Instant instant){
        log.debug(">>> projecting {} , timestamp : {}", event, instant.toString());
        HolderAccountSummary holderAccount = getHolderAccountSummary(event.getHolderID());
        holderAccount.setAccountCnt(holderAccount.getAccountCnt() + 1);
        repository.save(holderAccount);
    }

    @EventHandler
    @AllowReplay
    protected void on(DepositMoneyEvent event, @Timestamp Instant instant){
        log.debug(">>> projecting {} , timestamp : {}", event, instant.toString());
        HolderAccountSummary holderAccount = getHolderAccountSummary(event.getHolderID());
        holderAccount.setTotalBalance(holderAccount.getTotalBalance() + event.getAmount());
        repository.save(holderAccount);
    }

    @EventHandler
    @AllowReplay
    protected void on(WithdrawMoneyEvent event, @Timestamp Instant instant){
        log.debug(">>> projecting {} , timestamp : {}", event, instant.toString());
        HolderAccountSummary holderAccount = getHolderAccountSummary(event.getHolderID());
        holderAccount.setTotalBalance(holderAccount.getTotalBalance() - event.getAmount());
        repository.save(holderAccount);
    }

    private HolderAccountSummary getHolderAccountSummary(String holderID) {
        log.debug(">>> getHolder : {} ", holderID);
        return repository.findByHolderId(holderID)
                .orElseThrow(() -> new NoSuchElementException("소유주가 존재하지 않습니다."));
    }

    /**
     * 초기화 할 때 실행하는 부분
     */
    @ResetHandler
    private void resetHolderAccountInfo(){
        log.debug("reset triggered");
        repository.deleteAll();
    }
}