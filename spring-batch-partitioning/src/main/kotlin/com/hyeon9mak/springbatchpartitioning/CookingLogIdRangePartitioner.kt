package com.hyeon9mak.springbatchpartitioning

import org.springframework.batch.core.partition.Partitioner
import org.springframework.batch.infrastructure.item.ExecutionContext
import org.springframework.jdbc.core.JdbcTemplate
import java.time.Instant
import java.util.UUID

/**
 * Partitioner 는 CGLIB 를 사용하여 proxy 객체로 생성되므로, 상속(open)을 허용해야한다.
 */
open class CookingLogIdRangePartitioner(
    private val jdbcTemplate: JdbcTemplate,
    private val startDate: Instant,
    private val endDate: Instant,
) : Partitioner {

    override fun partition(gridSize: Int): Map<String, ExecutionContext> {
        val totalCount = countTargetData(startDate, endDate)
        if (totalCount == 0) {
            return emptyMap()
        }

        val actualGridSize = ((totalCount + PARTITION_SIZE - 1) / PARTITION_SIZE).coerceAtLeast(1)
        val boundaries = fetchPartitionBoundaries(actualGridSize, startDate, endDate)
        
        return boundaries.mapIndexed { index, boundary ->
            "partition$index" to ExecutionContext().apply {
                put("startCookedAt", boundary.startCookedAt)
                putString("startId", boundary.startId.toString())
                put("endCookedAt", boundary.endCookedAt)
                putString("endId", boundary.endId.toString())
            }
        }.toMap(LinkedHashMap())
    }

    private fun countTargetData(startDate: Instant, endDate: Instant): Int {
        val sql = """
            SELECT COUNT(*) 
            FROM cooking_log
            WHERE cooked_at >= ? AND cooked_at < ?
        """.trimIndent()

        return jdbcTemplate.queryForObject(sql, Int::class.java, startDate, endDate) ?: 0
    }

    private fun fetchPartitionBoundaries(gridSize: Int, startOfDay: Instant, endOfDay: Instant): List<PartitionBoundary> {
        val sql = """
            WITH partitioned AS (
                SELECT 
                    id,
                    cooked_at,
                    NTILE(?) OVER (ORDER BY cooked_at, id) AS bucket
                FROM cooking_log
                WHERE cooked_at >= ? AND cooked_at < ?
            )
            SELECT 
                MIN(cooked_at) AS start_cooked_at,
                MIN(id) AS start_id,
                MAX(cooked_at) AS end_cooked_at,
                MAX(id) AS end_id
            FROM partitioned
            GROUP BY bucket
            ORDER BY bucket
        """.trimIndent()

        return jdbcTemplate.query(sql, { rs, _ ->
            PartitionBoundary(
                startCookedAt = rs.getObject("start_cooked_at", Instant::class.java),
                startId = UUID.fromString(rs.getString("start_id")),
                endCookedAt = rs.getObject("end_cooked_at", Instant::class.java),
                endId = UUID.fromString(rs.getString("end_id"))
            )
        }, gridSize, startOfDay, endOfDay)
    }

    private data class PartitionBoundary(
        val startCookedAt: Instant,
        val startId: UUID,
        val endCookedAt: Instant,
        val endId: UUID
    )

    companion object {
        private const val PARTITION_SIZE = 10
    }
}
