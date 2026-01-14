package com.hyeon9mak.springbatchpartitioning

enum class CookingLogStatus {
    COOKED,
    ATE,
    ;

    companion object {
        fun findByName(name: String) = entries.find { e -> e.name == name }
            ?: throw IllegalArgumentException("No CookingLogStatus with name: $name")
    }
}
