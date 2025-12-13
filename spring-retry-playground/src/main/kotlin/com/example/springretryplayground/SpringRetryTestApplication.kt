package com.example.springretryplayground

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.retry.annotation.EnableRetry

@EnableRetry
@SpringBootApplication
class SpringRetryTestApplication

fun main(args: Array<String>) {
	runApplication<SpringRetryTestApplication>(*args)
}
