package com.hyeon9mak.springbatchpartitioning

import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.Step
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.infrastructure.item.Chunk
import org.springframework.batch.infrastructure.item.ItemProcessor
import org.springframework.batch.infrastructure.item.ItemReader
import org.springframework.batch.infrastructure.item.ItemWriter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.TaskExecutor
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.transaction.PlatformTransactionManager
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

@Configuration
class CookingLogUpdateJob {

    @Bean(JOB_NAME)
    fun job(
        jobRepository: JobRepository,
        eatStepManager: Step,
    ): Job {
        return JobBuilder(JOB_NAME, jobRepository)
            .start(eatStepManager)
            .preventRestart()
            .build()
    }

    @Bean
    fun cookingLogUpdateExecutor(): TaskExecutor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = POOL_SIZE
        executor.maxPoolSize = POOL_SIZE
        executor.setThreadNamePrefix("partition-thread")
        executor.setWaitForTasksToCompleteOnShutdown(true)
        executor.initialize()
        return executor
    }

    @Bean
    fun eatStepPartitionHandler(
        eatStep: Step,
        cookingLogUpdateExecutor: TaskExecutor,
    ): TaskExecutorPartitionHandler {
        val partitionHandler = TaskExecutorPartitionHandler()
        partitionHandler.step = eatStep
        partitionHandler.setTaskExecutor(cookingLogUpdateExecutor)
        partitionHandler.gridSize = POOL_SIZE
        return partitionHandler
    }

    @StepScope
    @Bean
    fun eatStepPartitioner(
        @Value("#{jobParameters['startDate']}") startDate: String?,
        @Value("#{jobParameters['endDate']}") endDate: String?,
        jdbcTemplate: JdbcTemplate,
    ): CookingLogIdRangePartitioner {
        requireNotNull(startDate) { "startDate job parameter is required" }
        requireNotNull(endDate) { "endDate job parameter is required" }

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

    @Bean
    fun eatStepManager(
        jobRepository: JobRepository,
        eatStepPartitioner: CookingLogIdRangePartitioner,
        partitionHandler: TaskExecutorPartitionHandler,
    ): Step {
        return StepBuilder("eat-step-manager", jobRepository)
            .partitioner(STEP_NAME, eatStepPartitioner)
            .partitionHandler(partitionHandler)
            .build()
    }

    @Bean
    fun eatStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
        eatableCookLogReader: ItemReader<EatableCookingLog>,
        ateCookingLogWriter: ItemWriter<AteCookingLog>,
    ): Step {
        return StepBuilder(STEP_NAME, jobRepository)
            .chunk<EatableCookingLog, AteCookingLog>(CHUNK_SIZE)
            .transactionManager(transactionManager)
            .reader(eatableCookLogReader)
            .processor(processor())
            .writer(ateCookingLogWriter)
            .build()
    }

    @Bean
    @StepScope
    fun eatableCookLogReader(
        @Value("#{stepExecutionContext['startCookedAt']}") startCookedAt: Instant,
        @Value("#{stepExecutionContext['startId']}") startId: UUID,
        @Value("#{stepExecutionContext['endCookedAt']}") endCookedAt: Instant,
        @Value("#{stepExecutionContext['endId']}") endId: UUID,
        jdbcTemplate: JdbcTemplate,
    ): ItemReader<EatableCookingLog> {
        return NoOffsetPagingItemReader(
            jdbcTemplate = jdbcTemplate,
            rowMapper = ROW_MAPPER,
            chunkSize = CHUNK_SIZE,
            startCookedAt = startCookedAt,
            startId = startId,
            endCookedAt = endCookedAt,
            endId = endId,
        )
    }

    private fun processor(): ItemProcessor<EatableCookingLog, AteCookingLog> {
        return ItemProcessor { it.eat() }
    }

    @Bean
    @StepScope
    fun ateCookingLogWriter(jdbcTemplate: JdbcTemplate): ItemWriter<AteCookingLog> {
        val sql = """
                UPDATE cooking_log
                SET status = ?
                WHERE id = ?
            """.trimIndent()

        return ItemWriter { items: Chunk<out AteCookingLog> ->
            jdbcTemplate.batchUpdate(sql, items.items.map { arrayOf(it.status.name, it.id.toString()) })
        }
    }

    companion object {
        private const val JOB_NAME = "cooking-log-update"
        private const val STEP_NAME = "eat-cooking-log-step"
        private const val CHUNK_SIZE = 10
        private const val POOL_SIZE = 5

        private val KST_ZONE_ID: ZoneId = ZoneId.of("Asia/Seoul")
        private val ROW_MAPPER = RowMapper { rs, _ ->
            EatableCookingLog(
                id = rs.getObject("id", UUID::class.java),
                status = CookingLogStatus.findByName(name = rs.getString("status")),
                cookedAt = rs.getObject("cooked_at", Instant::class.java),
            )
        }
    }
}
