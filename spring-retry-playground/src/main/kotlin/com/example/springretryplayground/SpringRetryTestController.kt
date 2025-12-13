package com.example.springretryplayground

import com.example.springretryplayground.retryable.SpringRecoverTestService
import com.example.springretryplayground.retryable.SpringRetryTestService
import com.example.springretryplayground.retrylistener.SpringRetryListenerTestService
import com.example.springretryplayground.retrytemplate.RetryTemplateService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class SpringRetryTestController(
    private val springRetryTestService: SpringRetryTestService,
    private val springRecoverTestService: SpringRecoverTestService,
    private val retryTemplateService: RetryTemplateService,
    private val springRetryListenerTestService: SpringRetryListenerTestService,

    ) {
    @GetMapping("/cats/{id}/image")
    fun catImage(@PathVariable id: Long): String = springRetryTestService.catImage(id = id)

    @GetMapping("/cats/{id}/recover-image")
    fun catImageRecover(@PathVariable id: Long): String = springRecoverTestService.catImage(id = id)

    @GetMapping("/cats/{id}/retry-template-image")
    fun catImageRetryTemplate(@PathVariable id: Long): String = retryTemplateService.catImage(id = id)

    @GetMapping("/cats/{id}/retry-listener-image")
    fun catImageRetryListener(@PathVariable id: Long): String = springRetryListenerTestService.catImage(id = id)
}