package com.hyeon9mak.springbatchpartitioning

import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.Step
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter


@Configuration
class CookingLogUpdateJob {

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

    companion object {
        private const val JOB_NAME = "cooking-log-update"
        private const val STEP_NAME = "eat-step"
        private const val STEP_MANAGER = "eat-step-manager"
        private val KST_ZONE_ID: ZoneId = ZoneId.of("Asia/Seoul")
    }
}
