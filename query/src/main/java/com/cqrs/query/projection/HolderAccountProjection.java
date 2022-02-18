package com.cqrs.query.projection;

import com.cqrs.event.AccountCreationEvent;
import com.cqrs.event.DepositMoneyEvent;
import com.cqrs.event.HolderCreationEvent;
import com.cqrs.event.WithdrawMoneyEvent;
import com.cqrs.query.entity.HolderAccountSummary;
import com.cqrs.query.query.AccountQuery;
import com.cqrs.query.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.AllowReplay;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.ResetHandler;
import org.axonframework.eventhandling.Timestamp;
import org.axonframework.queryhandling.QueryHandler;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.NoSuchElementException;

@Slf4j
@Component
@RequiredArgsConstructor
@ProcessingGroup("accounts")
public class HolderAccountProjection {
    private final AccountRepository repository;
    private final RetryTemplate retryTemplate;
    private final QueryUpdateEmitter queryUpdateEmitter;

    /**
     * EventHandler 메소드 파라미터에는 @Timestamp 외 @SequenceNumber, ReplayStatus 등이 추가로 전달될 수 있으며
     * 자세한 내용은 Axon 공식 문서를 참고 바랍니다.
     *
     * 계정 생성 시 초기값 설정하여 저장하는 부분
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

//retryTemplate로 적용
//    @EventHandler
//    @AllowReplay
//    protected void on(AccountCreationEvent event, @Timestamp Instant instant){
//        log.debug(">>> projecting {} , timestamp : {}", event, instant.toString());
//        retryTemplate.execute(context -> {
//            log.info("retry going on");
//            HolderAccountSummary holderAccount = getHolderAccountSummary(event.getHolderID());
//            holderAccount.setAccountCnt(holderAccount.getAccountCnt() + 1);
//            repository.save(holderAccount);
//            return null;
//        });
//    }

    @EventHandler
    @AllowReplay
    protected void on(DepositMoneyEvent event, @Timestamp Instant instant){
        log.debug(">>> projecting {} , timestamp : {}", event, instant.toString());
        HolderAccountSummary holderAccount = getHolderAccountSummary(event.getHolderID());
        holderAccount.setTotalBalance(holderAccount.getTotalBalance() + event.getAmount());

        queryUpdateEmitter.emit(AccountQuery.class,
                query -> query.getHolderId().equals(event.getHolderID()),
                holderAccount);

        repository.save(holderAccount);
    }

    @EventHandler
    @AllowReplay
    protected void on(WithdrawMoneyEvent event, @Timestamp Instant instant){
        log.debug(">>> projecting {} , timestamp : {}", event, instant.toString());
        HolderAccountSummary holderAccount = getHolderAccountSummary(event.getHolderID());
        holderAccount.setTotalBalance(holderAccount.getTotalBalance() - event.getAmount());

        //Subscription query 방식에서는 Read Model에 변경이 발생되었을 때 이를 전파해야합니다.
        queryUpdateEmitter.emit(AccountQuery.class,
                query -> query.getHolderId().equals(event.getHolderID()),
                holderAccount);

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
    public void resetHolderAccountInfo(){
        log.debug("reset triggered");
        repository.deleteAll();
    }

    @QueryHandler
    public HolderAccountSummary on3(AccountQuery query){
        log.debug(">>> handling fake {}", query);
        HolderAccountSummary res = new HolderAccountSummary();
        res.setName("test");
        return res;
    }

    @QueryHandler
    public HolderAccountSummary on2(AccountQuery query){
        log.debug(">>> handling queryHandler {}", query);
        return repository.findByHolderId(query.getHolderId()).orElse(null);
    }

}