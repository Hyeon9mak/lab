package com.hyeon9mak.springkafka

import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.KafkaAdmin

@Configuration
class KafkaConfig {

    /**
     * KafkaAdmin Bean 설정
     * 별도 옵션을 설정하고 싶다면 이를 통해 진행하면 됨
     * 생략 가능
     */
    @Bean
    fun admin(): KafkaAdmin = KafkaAdmin(mapOf(
        AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:19092,localhost:39092,localhost:49092"),
    )

    /**
     * Topic 자동 생성 설정
     * spring-kafka 2.6 버전부터는 partitions 이나 replicas 설정이 없으면 Kafka Broker 의 기본 설정을 따름
     * (단, kafka 2.4 이상 버전부터 유효한 방식)
     */
    @Bean
    fun kafkaStudyTopic(): NewTopic = TopicBuilder.name("kafka-study-topic")
        .partitions(3)
        .replicas(3)
        .compact()
        .build()
}
