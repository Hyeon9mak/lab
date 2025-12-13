package com.example.springretryplayground.retrytemplate

import com.example.springretryplayground.retrylistener.CatImageListener
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.retry.support.RetryTemplate

@Configuration
class RetryTemplateConfig {

    @Bean
    fun retryTemplate(): RetryTemplate = RetryTemplate.builder()
        .maxAttempts(3)
        .fixedBackoff(1000)
        .retryOn(RuntimeException::class.java)
        .withListeners(listOf(CatImageListener()))
        .build()
}