package com.hyeon9mak.springbatchpartitioning

import java.time.Instant
import java.util.UUID

class UpdatableCookingLog(
    val id: UUID,
    val name: String,
    val description: String,
    val status: CookingLogStatus,
    val cookedAt: Instant,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UpdatableCookingLog

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
