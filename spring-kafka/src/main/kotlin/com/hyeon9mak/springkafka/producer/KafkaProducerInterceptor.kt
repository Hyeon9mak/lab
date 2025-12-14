package com.hyeon9mak.springkafka.producer

import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.kafka.clients.producer.ProducerInterceptor
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.header.Headers
import java.util.*

class KafkaProducerInterceptor : ProducerInterceptor<UUID, String> {
    override fun onSend(producerRecord: ProducerRecord<UUID, String>): ProducerRecord<UUID, String> {
        LOGGER.info { "onSend: topic=${producerRecord.topic()}, partition=${producerRecord.partition()}" }
        return producerRecord
    }

    override fun onAcknowledgement(metadata: RecordMetadata, exception: Exception?, headers: Headers) {
        if (exception == null) {
            LOGGER.info { "onAcknowledgement succeeded: topic=${metadata.topic()}, partition=${metadata.partition()}" }
        } else {
            LOGGER.error(exception) { "onAcknowledgement failed: topic=${metadata.topic()}, partition=${metadata.partition()}" }
        }
    }

    override fun configure(p0: MutableMap<String, *>?) {}

    override fun close() {}

    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }
}
