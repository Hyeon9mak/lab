package com.hyeon9mak.springbatchmultithreadedstep

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import kotlin.system.exitProcess

@SpringBootApplication
class SpringBatchMultiThreadedStepApplication

fun main(args: Array<String>) {
    val context = runApplication<SpringBatchMultiThreadedStepApplication>(*args)
    val statusCode = SpringApplication.exit(context)
    LOGGER.info { "Batch Application Exit with Code: $statusCode" }
    exitProcess(status = statusCode)
}

private val LOGGER = mu.KotlinLogging.logger {}
