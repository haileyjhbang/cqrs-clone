package com.cqrs.query.service;

import com.cqrs.query.entity.HolderAccountSummary;
import com.cqrs.query.repository.AccountRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class RetryServiceTest {

    @Autowired
    private RetryService retryService;
    @MockBean
    private AccountRepository accountRepository;

    @Test
    void retry_with_bean(){
        //when
        Mockito.when(accountRepository.findByHolderId(ArgumentMatchers.anyString())).thenThrow(RuntimeException.class);
        //assertThrows(RuntimeException.class, () -> retryService.getHolderAccountSummary(ArgumentMatchers.anyString()));
        retryService.getHolderAccountSummary(ArgumentMatchers.anyString());

        //then
        Mockito.verify(accountRepository, Mockito.times(3)).findByHolderId(ArgumentMatchers.anyString());
    }

    @Test
    void recovery_with_bean(){
        //when
        Mockito.when(accountRepository.findByHolderId(ArgumentMatchers.anyString())).thenThrow(RuntimeException.class);
        HolderAccountSummary result = retryService.getHolderAccountSummary(ArgumentMatchers.anyString());
        //then
        Mockito.verify(accountRepository, Mockito.times(3)).findByHolderId(ArgumentMatchers.anyString());
        Assertions.assertThat(result.getName()).isEqualTo("recovery");
    }
}