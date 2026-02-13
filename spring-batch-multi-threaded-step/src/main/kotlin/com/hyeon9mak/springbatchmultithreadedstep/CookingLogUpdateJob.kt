package com.hyeon9mak.springbatchmultithreadedstep

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
import org.springframework.batch.infrastructure.item.database.Order
import org.springframework.batch.infrastructure.item.database.builder.JdbcPagingItemReaderBuilder
import org.springframework.batch.infrastructure.item.database.support.MySqlPagingQueryProvider
import org.springframework.batch.infrastructure.item.support.builder.SynchronizedItemStreamReaderBuilder
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
import javax.sql.DataSource

@Configuration
class CookingLogUpdateJob {

    @Bean(JOB_NAME)
    fun job(
        jobRepository: JobRepository,
        eatStep: Step,
    ): Job {
        return JobBuilder(JOB_NAME, jobRepository)
            .start(eatStep)
            .preventRestart()
            .build()
    }

    @Bean
    fun cookingLogUpdateExecutor(): TaskExecutor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = POOL_SIZE
        executor.maxPoolSize = POOL_SIZE
        executor.setThreadNamePrefix("multi-threaded-step-")
        executor.setWaitForTasksToCompleteOnShutdown(true)
        executor.initialize()
        return executor
    }

    @Bean
    fun eatStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
        cookingLogUpdateExecutor: TaskExecutor,
        eatableCookLogReader: ItemReader<EatableCookingLog>,
        eatCookLogProcessor: ItemProcessor<EatableCookingLog, AteCookingLog>,
        ateCookingLogWriter: ItemWriter<AteCookingLog>,
    ): Step {
        return StepBuilder(STEP_NAME, jobRepository)
            .chunk<EatableCookingLog, AteCookingLog>(CHUNK_SIZE, transactionManager)
            .reader(eatableCookLogReader)
            .processor(eatCookLogProcessor)
            .writer(ateCookingLogWriter)
            .taskExecutor(cookingLogUpdateExecutor)
            .build()
    }

    @StepScope
    @Bean
    fun eatableCookLogReader(
        @Value("#{jobParameters['startCookedAt']}") startCookedAtString: String,
        @Value("#{jobParameters['endCookedAt']}") endCookedAtString: String,
        dataSource: DataSource,
    ): ItemReader<EatableCookingLog> {
        val startDateInstant = LocalDate.parse(startCookedAtString, DateTimeFormatter.ISO_LOCAL_DATE)
            .atStartOfDay(KST_ZONE_ID)
            .toInstant()
        val endDateInstant = LocalDate.parse(endCookedAtString, DateTimeFormatter.ISO_LOCAL_DATE)
            .atStartOfDay(KST_ZONE_ID)
            .toInstant()

        val pagingItemReader = JdbcPagingItemReaderBuilder<EatableCookingLog>()
            .dataSource(dataSource)
            .fetchSize(CHUNK_SIZE)
            .rowMapper { rs, rowNum ->
                ROW_MAPPER.mapRow(rs, rowNum).also {
                    LOGGER.info { "[${Thread.currentThread().name}] Read cooking log: id=${it.id}, cookedAt=${it.cookedAt}" }
                }
            }
            .queryProvider(
                MySqlPagingQueryProvider().apply {
                    setSelectClause("id, status, cooked_at")
                    setFromClause("from cooking_log")
                    setWhereClause("where status = 'COOKED' and cooked_at >= :startCookedAt and cooked_at < :endCookedAt")
                    setSortKeys(
                        mapOf(
                            "cooked_at" to Order.ASCENDING,
                            "id" to Order.ASCENDING,
                        )
                    )
                }
            )
            .parameterValues(
                mapOf(
                    "startCookedAt" to startDateInstant,
                    "endCookedAt" to endDateInstant,
                )
            )
            .name("eatableCookLogReader")
            .saveState(false)
            .build()

        return SynchronizedItemStreamReaderBuilder<EatableCookingLog>()
            .delegate(pagingItemReader)
            .build()
    }

    @Bean
    fun eatCookLogProcessor(): ItemProcessor<EatableCookingLog, AteCookingLog> {
        return ItemProcessor {
            it.eat().also { LOGGER.info { "[${Thread.currentThread().name}] ate food. ${it.id}" } }
        }
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
                .also { LOGGER.info { "[${Thread.currentThread().name}] Wrote ate cooking logs to database. ${items.map { it.id }}" } }
        }
    }

    companion object {
        private const val JOB_NAME = "cooking-log-update"
        private const val STEP_NAME = "eat-cooking-log-step"
        private const val CHUNK_SIZE = 10
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
