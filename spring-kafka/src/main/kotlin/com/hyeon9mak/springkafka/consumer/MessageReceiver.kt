package com.hyeon9mak.springkafka.consumer

import com.hyeon9mak.springkafka.Topics.KAFKA_STUDY_TOPIC
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Component
import java.util.*

@Component
class MessageReceiver {

    @KafkaListener(topics = [KAFKA_STUDY_TOPIC])
    fun receiveMessage(
        @Header(KafkaHeaders.RECEIVED_KEY) key: UUID,
        message: String,
    ) {
        LOGGER.info { "Received message: key=$key, value=$message" }
        ReceivedMessageRepository.save(key = key, message = message)
    }

    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }
}
