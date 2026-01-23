package com.hyeon9mak.springbatchasyncitemprocessor

import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.Step
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.infrastructure.item.Chunk
import org.springframework.batch.infrastructure.item.ItemProcessor
import org.springframework.batch.infrastructure.item.ItemReader
import org.springframework.batch.infrastructure.item.ItemWriter
import org.springframework.batch.integration.async.AsyncItemProcessor
import org.springframework.batch.integration.async.AsyncItemWriter
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
import java.util.concurrent.Future

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
        executor.setThreadNamePrefix("async-processor-")
        executor.setWaitForTasksToCompleteOnShutdown(true)
        executor.initialize()
        return executor
    }

    @Bean
    fun eatStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
        eatableCookLogReader: ItemReader<EatableCookingLog>,
        asyncItemProcessor: AsyncItemProcessor<EatableCookingLog, AteCookingLog>,
        ateCookingLogAsyncWriter: AsyncItemWriter<AteCookingLog>,
    ): Step {
        return StepBuilder(STEP_NAME, jobRepository)
            .chunk<EatableCookingLog, Future<AteCookingLog>>(CHUNK_SIZE)
            .transactionManager(transactionManager)
            .reader(eatableCookLogReader)
            .processor(asyncItemProcessor)
            .writer(ateCookingLogAsyncWriter)
            .build()
    }

    @StepScope
    @Bean
    fun eatableCookLogReader(
        @Value("#{jobParameters['startCookedAt']}") startCookedAtString: String,
        @Value("#{jobParameters['endCookedAt']}") endCookedAtString: String,
        jdbcTemplate: JdbcTemplate,
    ): ItemReader<EatableCookingLog> {
        val startDateInstant = LocalDate.parse(startCookedAtString, DateTimeFormatter.ISO_LOCAL_DATE)
            .atStartOfDay(KST_ZONE_ID)
            .toInstant()
        val endDateInstant = LocalDate.parse(endCookedAtString, DateTimeFormatter.ISO_LOCAL_DATE)
            .atStartOfDay(KST_ZONE_ID)
            .toInstant()

        return NoOffsetPagingItemReader(
            jdbcTemplate = jdbcTemplate,
            rowMapper = ROW_MAPPER,
            chunkSize = CHUNK_SIZE,
            startCookedAt = startDateInstant,
            endCookedAt = endDateInstant,
        ).also {
            LOGGER.info { "[${Thread.currentThread().name}] ItemReader initialized - Range: cookedAt=[$startDateInstant ~ $endDateInstant]" }
        }
    }

    @Bean
    fun asyncItemProcessor(): AsyncItemProcessor<EatableCookingLog, AteCookingLog> {
        val processor = ItemProcessor<EatableCookingLog, AteCookingLog> {
            it.eat().also { LOGGER.info { "[${Thread.currentThread().name}] ate food." } }
        }
        val asyncItemProcessor = AsyncItemProcessor(processor)
        asyncItemProcessor.setTaskExecutor(cookingLogUpdateExecutor())
        return asyncItemProcessor
    }

    @Bean
    fun ateCookingLogAsyncWriter(
        ateCookingLogWriter: ItemWriter<AteCookingLog>,
    ): AsyncItemWriter<AteCookingLog> {
        return AsyncItemWriter(ateCookingLogWriter)
    }

    @Bean
    fun ateCookingLogWriter(jdbcTemplate: JdbcTemplate): ItemWriter<AteCookingLog> {
        val sql = """
                UPDATE cooking_log
                SET status = ?
                WHERE id = ?
            """.trimIndent()

        return ItemWriter { items: Chunk<out AteCookingLog> ->
            jdbcTemplate.batchUpdate(sql, items.items.map { arrayOf(it.status.name, it.id.toString()) })
                .also { LOGGER.info { "[${Thread.currentThread().name}] Wrote ate cooking logs to database." } }
        }
    }

    companion object {
        private const val JOB_NAME = "cooking-log-update"
        private const val STEP_NAME = "eat-cooking-log-step"
        private const val CHUNK_SIZE = 100
        private const val POOL_SIZE = 5

        private val LOGGER = mu.KotlinLogging.logger {}
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
