package com.hyeon9mak.springkafka

import com.hyeon9mak.springkafka.consumer.MessageReceiver
import com.hyeon9mak.springkafka.consumer.ReceivedMessageRepository
import com.hyeon9mak.springkafka.producer.MessageSender
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.lang.Thread.sleep
import java.util.*

@SpringBootTest
class SpringKafkaApplicationTests @Autowired constructor(
    private val messageSender: MessageSender,
    private val messageReceiver: MessageReceiver,
) {

    @Test
    fun sendAndReceiveTest() {
        val key = UUID.randomUUID()
        val message = "Hello, Kafka study members!"

        messageSender.sendMessage(key = key, message = message)
        sleep(1_000) // wait for the message to be processed

        val receivedMessage = ReceivedMessageRepository.findByKey(key = key)
        assertThat(receivedMessage).isEqualTo(message)
    }
}
