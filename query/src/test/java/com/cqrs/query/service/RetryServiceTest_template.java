package com.cqrs.query.service;

import com.cqrs.query.entity.HolderAccountSummary;
import com.cqrs.query.repository.AccountRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@ExtendWith(MockitoExtension.class)
public class RetryServiceTest_template {
    @InjectMocks
    RetryService retryService;
    @Mock
    AccountRepository accountRepository;
//@SpringBootTest
//class RetryServiceTest_template {
//
//    @Autowired
//    private RetryService retryService;
//    @MockBean
//    private AccountRepository accountRepository;

    @Test
    void retryTest_with_template(){
        Mockito.when(accountRepository.findByHolderId(ArgumentMatchers.anyString())).thenThrow(RuntimeException.class);
        Assertions.assertThrows(RuntimeException.class, () -> retryService.getHolderAccountSummaryTemplate(ArgumentMatchers.anyString()));

        Mockito.verify(accountRepository, Mockito.times(3)).findByHolderId(ArgumentMatchers.anyString());
    }

    @Test
    void recovery_with_template(){
        //when
        Mockito.when(accountRepository.findByHolderId(ArgumentMatchers.anyString())).thenThrow(RuntimeException.class);
        HolderAccountSummary result = retryService.getHolderAccountSummaryTemplateAndRecovery(ArgumentMatchers.anyString());
        //then
        Mockito.verify(accountRepository, Mockito.times(3)).findByHolderId(ArgumentMatchers.anyString());
        org.assertj.core.api.Assertions.assertThat(result.getName()).isEqualTo("recovery");
    }
}
