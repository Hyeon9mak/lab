package com.hyeon9mak.springkafkarollingupgrade

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SpringKafkaRollingUpgradeApplication

fun main(args: Array<String>) {
    runApplication<SpringKafkaRollingUpgradeApplication>(*args)
}
