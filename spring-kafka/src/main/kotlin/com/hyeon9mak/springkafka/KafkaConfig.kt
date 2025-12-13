package com.hyeon9mak.springkafka

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder

@Configuration
class KafkaConfig {

    /**
     * Topic 자동 생성 설정
     * spring-kafka 2.6 버전부터는 partitions 이나 replicas 설정이 없으면 Kafka Broker 의 기본 설정을 따름
     * (단, kafka 2.4 이상 버전부터 유효한 방식)
     */
    @Bean
    fun topic1(): NewTopic = TopicBuilder.name("thing1")
        .partitions(2)
        .replicas(3)
        .compact()
        .build()
}
