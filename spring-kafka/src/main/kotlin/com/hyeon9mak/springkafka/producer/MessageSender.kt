package com.hyeon9mak.springkafka.producer

import com.hyeon9mak.springkafka.Topics.KAFKA_STUDY_TOPIC
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.util.*

@Component
class MessageSender(
    private val kafkaTemplate: KafkaTemplate<Any, Any>,
) {
    fun sendMessage(key: UUID, message: String) {
        kafkaTemplate.send(KAFKA_STUDY_TOPIC, key, message)
        LOGGER.info { "Sent message: key=$key, value=$message" }
    }

    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }
}
