package com.hyeon9mak.springbatchpartitioning

import org.springframework.batch.core.partition.Partitioner
import org.springframework.batch.infrastructure.item.ExecutionContext
import org.springframework.jdbc.core.JdbcTemplate
import java.time.Instant
import java.util.UUID

class CookingLogIdRangePartitioner(
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
                put("startCreatedAt", boundary.startCreatedAt)
                putString("startId", boundary.startId.toString())
                put("endCreatedAt", boundary.endCreatedAt)
                putString("endId", boundary.endId.toString())
            }
        }.toMap(LinkedHashMap())
    }

    private fun countTargetData(startDate: Instant, endDate: Instant): Int {
        val sql = """
            SELECT COUNT(*) 
            FROM cooking_log
            WHERE created_at >= ? AND created_at < ?
        """.trimIndent()

        return jdbcTemplate.queryForObject(sql, Int::class.java, startDate, endDate) ?: 0
    }

    private fun fetchPartitionBoundaries(gridSize: Int, startOfDay: Instant, endOfDay: Instant): List<PartitionBoundary> {
        val sql = """
            WITH partitioned AS (
                SELECT 
                    id,
                    created_at,
                    NTILE(?) OVER (ORDER BY created_at, id) AS bucket
                FROM your_table
                WHERE created_at >= ? AND created_at < ?
            )
            SELECT 
                MIN(created_at) AS start_created_at,
                MIN(id) AS start_id,
                MAX(created_at) AS end_created_at,
                MAX(id) AS end_id
            FROM partitioned
            GROUP BY bucket
            ORDER BY bucket
        """.trimIndent()

        return jdbcTemplate.query(sql, { rs, _ ->
            PartitionBoundary(
                startCreatedAt = rs.getObject("start_created_at", Instant::class.java),
                startId = UUID.fromString(rs.getString("start_id")),
                endCreatedAt = rs.getObject("end_created_at", Instant::class.java),
                endId = UUID.fromString(rs.getString("end_id"))
            )
        }, gridSize, startOfDay, endOfDay)
    }

    private data class PartitionBoundary(
        val startCreatedAt: Instant,
        val startId: UUID,
        val endCreatedAt: Instant,
        val endId: UUID
    )

    companion object {
        private const val PARTITION_SIZE = 100_000
    }
}
