package com.hyeon9mak.springbatchpartitioning

import java.util.UUID

class EatableCookingLog(
    val id: UUID,
    val status: CookingLogStatus,
) {
    fun eat(): AteCookingLog {
        return AteCookingLog(
            id = id,
            status = CookingLogStatus.ATE,
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EatableCookingLog

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}

class AteCookingLog(
    val id: UUID,
    val status: CookingLogStatus,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AteCookingLog

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
