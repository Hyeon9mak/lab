package com.hyeon9mak.springkafka.consumer

import java.util.*

object ReceivedMessageRepository {

    private val receivedMessages = mutableMapOf<UUID, String>()

    fun save(key: UUID, message: String) {
        receivedMessages[key] = message
    }

    fun findByKey(key: UUID): String {
        return receivedMessages[key]
            ?: throw NoSuchElementException("No message found for key: $key")
    }
}
