package com.example.springretryplayground.retrylistener

import org.slf4j.LoggerFactory
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Recover
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service

@Service
class SpringRetryListenerTestService {

    @Retryable(
        maxAttempts = 3,
        backoff = Backoff(delay = 1000),
        include = [RuntimeException::class],
        listeners = ["catImageListener"]
    )
    fun catImage(id: Long): String {
        throw RuntimeException("Failed to get cat image.")
    }

    @Recover
    fun recoverListener(e: RuntimeException, id: Long): String {
        logger.info("recover: $e")
        return "very_cute_cat_image.png"
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SpringRetryListenerTestService::class.java)
    }
}