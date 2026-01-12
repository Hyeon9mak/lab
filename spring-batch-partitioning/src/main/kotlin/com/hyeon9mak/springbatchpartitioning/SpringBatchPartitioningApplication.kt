package com.hyeon9mak.springbatchpartitioning

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import kotlin.system.exitProcess

@SpringBootApplication
class SpringBatchPartitioningApplication

fun main(args: Array<String>) {
    val context = runApplication<SpringBatchPartitioningApplication>(*args)
    val statusCode = SpringApplication.exit(context)
    LOGGER.info { "Batch Application Exit with Code: $statusCode" }
    exitProcess(status = statusCode)
}

private val LOGGER = mu.KotlinLogging.logger {}
