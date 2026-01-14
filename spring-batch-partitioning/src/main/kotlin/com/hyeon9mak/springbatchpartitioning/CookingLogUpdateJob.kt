package com.hyeon9mak.springbatchpartitioning

import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.Step
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.infrastructure.item.Chunk
import org.springframework.batch.infrastructure.item.ItemProcessor
import org.springframework.batch.infrastructure.item.ItemWriter
import org.springframework.batch.infrastructure.item.database.JdbcCursorItemReader
import org.springframework.batch.infrastructure.item.database.builder.JdbcCursorItemReaderBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.TaskExecutor
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.sql.DataSource


@Configuration
class CookingLogUpdateJob {

    @Bean("$JOB_NAME-task-pool")
    fun executor(): TaskExecutor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = POOL_SIZE
        executor.maxPoolSize = POOL_SIZE
        executor.setThreadNamePrefix("partition-thread")
        executor.setWaitForTasksToCompleteOnShutdown(true)
        executor.initialize()
        return executor
    }

    @Bean("$STEP_NAME-partitioner")
    @StepScope
    fun partitioner(
        @Value("#{jobParameters['startDate']}") startDate: String,
        @Value("#{jobParameters['endDate']}") endDate: String,
        jdbcTemplate: JdbcTemplate,
    ): CookingLogIdRangePartitioner {
        val startDateInstant = LocalDate.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE)
            .atStartOfDay(KST_ZONE_ID)
            .toInstant()
        val endDateInstant = LocalDate.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE)
            .atStartOfDay(KST_ZONE_ID)
            .toInstant()
        return CookingLogIdRangePartitioner(
            jdbcTemplate = jdbcTemplate,
            startDate = startDateInstant,
            endDate = endDateInstant,
        )
    }

    @Bean(STEP_NAME)
    fun eatStep(jobRepository: JobRepository): Step {
        return StepBuilder(jobRepository)
            .chunk<EatableCookingLog, AteCookingLog>(CHUNK_SIZE)
            .reader(reader(null, null, null, null, null))
            .processor(processor())
            .writer(writer(null))
            .build()
    }

    @Bean("$STEP_NAME-reader")
    @StepScope
    fun reader(
        @Value("#{stepExecutionContext['startCookedAt']}") startCookedAt: Instant,
        @Value("#{stepExecutionContext['startId']}") startId: String,
        @Value("#{stepExecutionContext['endCookedAt']}") endCookedAt: Instant,
        @Value("#{stepExecutionContext['endId']}") endId: String,
        dataSource: DataSource,
    ): JdbcCursorItemReader<EatableCookingLog> {
        val sql = """
        SELECT 
            id, status
        FROM
            cooking_log
        WHERE (cooked_at, id) >= (?, ?::uuid)
          AND (cooked_at, id) <= (?, ?::uuid)
        ORDER BY cooked_at, id
    """.trimIndent()

        return JdbcCursorItemReaderBuilder<EatableCookingLog>()
            .name("partitionReader")
            .dataSource(dataSource)
            .sql(sql)
            .preparedStatementSetter { ps ->
                ps.setObject(1, startCookedAt)
                ps.setString(2, startId)
                ps.setObject(3, endCookedAt)
                ps.setString(4, endId)
            }
            .fetchSize(CHUNK_SIZE)
            .rowMapper(ROW_MAPPER)
            .build()
    }

    private fun processor(): ItemProcessor<EatableCookingLog, AteCookingLog> {
        return ItemProcessor { it.eat() }
    }

    @Bean("$STEP_NAME-writer")
    @StepScope
    fun writer(jdbcTemplate: JdbcTemplate): ItemWriter<AteCookingLog> {
        val sql = """
                UPDATE cooking_log
                SET status = ?
                WHERE id = ?::uuid
            """.trimIndent()

        return ItemWriter { items: Chunk<out AteCookingLog> ->
            jdbcTemplate.batchUpdate(sql, items.items.map { arrayOf(it.status.name, it.id.toString()) })
        }
    }

    companion object {
        private const val JOB_NAME = "cooking-log-update"
        private const val STEP_NAME = "eat-step"
        private const val STEP_MANAGER = "eat-step-manager"
        private const val CHUNK_SIZE = 100_000
        private const val POOL_SIZE = 5

        private val KST_ZONE_ID: ZoneId = ZoneId.of("Asia/Seoul")
        private val ROW_MAPPER = RowMapper { rs, _ ->
            EatableCookingLog(
                id = rs.getObject("id", UUID::class.java),
                status = CookingLogStatus.findByName(name = rs.getString("status")),
            )

        }
    }
}
