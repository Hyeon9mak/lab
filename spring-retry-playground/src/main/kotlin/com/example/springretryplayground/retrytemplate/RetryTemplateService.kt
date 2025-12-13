package com.example.springretryplayground.retrytemplate

import org.springframework.retry.support.RetryTemplate
import org.springframework.stereotype.Service

@Service
class RetryTemplateService(
    private val sampleRetryService: SampleRetryService,
    private val retryTemplate: RetryTemplate,
) {
    fun catImage(id: Long): String {
        return retryTemplate.execute<String, RuntimeException>(
            { sampleRetryService.catImage(id = id) },
            { sampleRetryService.recover(id = id) }
        )
    }
}