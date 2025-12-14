package com.hyeon9mak.springkafka.consumer

import com.hyeon9mak.springkafka.Topics.KAFKA_STUDY_TOPIC
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class MessageReceiver {

    @KafkaListener(topics = [KAFKA_STUDY_TOPIC])
    fun receiveMessage(key: Int, value: String) {
        LOGGER.info { "Received message: key=$key, value=$value" }
    }

    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }
}
