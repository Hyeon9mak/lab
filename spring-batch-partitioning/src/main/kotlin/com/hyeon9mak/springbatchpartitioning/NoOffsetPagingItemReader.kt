package com.hyeon9mak.springbatchpartitioning

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
    startId: UUID,
    private val endCookedAt: Instant,
    private val endId: UUID,
) : ItemReader<EatableCookingLog> {
    /**
     * 현재 구조에서는 startCookedAt, startId 만으로 pagination 을 수행하고 있다.
     * 그러나 endCookedAt, endId 가 고정되어 있어, pagination 이 충분히 수행되기 전까지 scan 범위가 넓다는 단점이 존재한다.
     *
     * startCookedAt, startId 과 마찬가지로 endCookedAt, endId 도 현행화해서 관리할 경우
     * 첫 page 부터 chunk size 만큼의 scan 만 진행하기 때문에 속도면에서 훨씬 효율적이다.
     *
     * 그러나, 첫 번째 페이지와 마지막 페이지에 대한 추가 조건문이 필요해 코드 복잡도가 증가한다.
     * partitioning 및 pagination 은 그 자체적으로 대량의 데이터 조회를 효율적으로 처리하기 위한 기법이므로,
     * 이미 충분한 범위 축소가 이루어졌다고 가정하고 단순화를 선택했다.
     */
    private var lastCookedAt: Instant = startCookedAt
    private var lastId: UUID = startId
    private var buffer: Iterator<EatableCookingLog> = emptyList<EatableCookingLog>().iterator()

    override fun read(): EatableCookingLog? {
        if (buffer.notHasNext()) {
            fetchNextChunk()
        }
        return if (buffer.hasNext()) buffer.next() else null
    }

    private fun Iterator<Any>.notHasNext(): Boolean = !this.hasNext()

    private fun fetchNextChunk() {
        val sql = """
            SELECT id, status, cooked_at
            FROM cooking_log
            WHERE status = 'COOKED'
              AND (cooked_at, id) >= (?, ?)
              AND (cooked_at, id) <= (?, ?)
            ORDER BY cooked_at, id
            LIMIT ?
        """.trimIndent()

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
        }

        buffer = items.iterator()
    }
}
