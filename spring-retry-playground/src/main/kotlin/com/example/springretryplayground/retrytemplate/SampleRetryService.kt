package com.example.springretryplayground.retrytemplate

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class SampleRetryService {

    fun catImage(id: Long): String {
        logger.info("exception will be thrown.")
        throw RuntimeException("Failed to get cat image.")
    }

    fun recover(id: Long): String {
        return "very_cute_cat_image.png"
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SampleRetryService::class.java)
    }
}