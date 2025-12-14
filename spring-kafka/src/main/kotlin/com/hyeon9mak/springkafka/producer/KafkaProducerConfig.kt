package com.hyeon9mak.springkafka.producer

import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.common.serialization.UUIDSerializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory

@Configuration
class KafkaProducerConfig {

    @Bean
    fun kafkaTemplate(): KafkaTemplate<Any, Any> {
        return KafkaTemplate(producerFactory())
    }

    @Bean
    fun producerFactory(): ProducerFactory<Any, Any> {
        return DefaultKafkaProducerFactory(producerConfigs())
    }

    @Bean
    fun producerConfigs(): MutableMap<String, Any> {
        val props = mutableMapOf<String, Any>()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:29092,localhost:39092,localhost:49092"
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = UUIDSerializer::class.java
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        props[ProducerConfig.INTERCEPTOR_CLASSES_CONFIG] = listOf(KafkaProducerInterceptor::class.java.canonicalName)
        return props
    }
}
