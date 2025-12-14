package com.hyeon9mak.springkafka.producer

import com.hyeon9mak.springkafka.Topics.KAFKA_STUDY_TOPIC
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class MessageSender(
    private val kafkaTemplate: KafkaTemplate<Any, Any>,
) {
    fun sendMessage(key: Int, value: String) {
        kafkaTemplate.send(KAFKA_STUDY_TOPIC, key, value)
        LOGGER.info { "Sent message: key=$key, value=$value" }
    }

    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }
}
