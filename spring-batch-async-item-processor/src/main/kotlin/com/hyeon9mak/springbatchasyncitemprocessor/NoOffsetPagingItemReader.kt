package com.hyeon9mak.springbatchasyncitemprocessor

import org.springframework.batch.infrastructure.item.ItemReader
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import java.time.Instant
import java.util.UUID

class NoOffsetPagingItemReader(
    private val jdbcTemplate: JdbcTemplate,
    private val rowMapper: RowMapper<EatableCookingLog>,
    private val chunkSize: Int,
    startCookedAt: Instant,
    private val endCookedAt: Instant,
) : ItemReader<EatableCookingLog> {
    /**
     * No-offset 기반 페이징: (cooked_at, id) 튜플 커서를 사용
     * ID 경계는 최소/최대값으로 자동 설정하여 시간 범위 내 모든 데이터 포함
     */
    private var lastCookedAt: Instant = startCookedAt
    private var lastId: UUID = UUID(0, 0)
    private val endId: UUID = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff")
    private var buffer: Iterator<EatableCookingLog> = emptyList<EatableCookingLog>().iterator()
    private var isFirstFetch: Boolean = true

    override fun read(): EatableCookingLog? {
        if (buffer.notHasNext()) {
            fetchNextChunk()
        }
        return if (buffer.hasNext()) buffer.next() else null
    }

    private fun Iterator<Any>.notHasNext(): Boolean = !this.hasNext()

    /**
     * 첫 fetch: start boundary inclusive (>=)로 데이터 조회,
     * 이후 fetch: 마지막으로 읽은 row를 제외하기 위해 strict greater than (>) 사용
     */
    private fun fetchNextChunk() {
        LOGGER.info { "[${Thread.currentThread().name}] Fetching chunk - Current cursor: cookedAt=$lastCookedAt, id=$lastId, End boundary: cookedAt=$endCookedAt, id=$endId, isFirstFetch=$isFirstFetch" }

        val sql = if (isFirstFetch) {
            """
                SELECT id, status, cooked_at
                FROM cooking_log
                WHERE status = 'COOKED'
                  AND (cooked_at, id) >= (?, ?)
                  AND (cooked_at, id) <= (?, ?)
                ORDER BY cooked_at, id
                LIMIT ?
            """.trimIndent()
        } else {
            """
                SELECT id, status, cooked_at
                FROM cooking_log
                WHERE status = 'COOKED'
                  AND (cooked_at, id) > (?, ?)
                  AND (cooked_at, id) <= (?, ?)
                ORDER BY cooked_at, id
                LIMIT ?
            """.trimIndent()
        }.also { isFirstFetch = false }

        val items = jdbcTemplate.query(
            sql,
            rowMapper,
            lastCookedAt,
            lastId.toString(),
            endCookedAt,
            endId.toString(),
            chunkSize
        )

        if (items.isNotEmpty()) {
            val lastItem = items.last()
            lastCookedAt = lastItem.cookedAt
            lastId = lastItem.id
            LOGGER.info { "[${Thread.currentThread().name}] Fetched ${items.size} items - Next cursor: cookedAt=$lastCookedAt, id=$lastId" }
        } else {
            LOGGER.info { "[${Thread.currentThread().name}] No more items to fetch - Partition reading completed" }
        }

        buffer = items.iterator()
    }

    companion object {
        private val LOGGER = mu.KotlinLogging.logger {}
    }
}
