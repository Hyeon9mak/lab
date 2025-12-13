package com.example.springretryplayground.retryable

import org.slf4j.LoggerFactory
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service

@Service
class SpringRetryTestService {

    private var counter = 0

    @Retryable(
        maxAttempts = 3,
        backoff = Backoff(delay = 1000),
        include = [RuntimeException::class]
    )
    fun catImage(id: Long): String {
        counter += 1
        logger.info("counter: $counter")

        if (counter % 2 == 0) {
            return "cat_${id}_image.png"
        }

        throw RuntimeException("Failed to get cat image.")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SpringRetryTestService::class.java)
    }
}