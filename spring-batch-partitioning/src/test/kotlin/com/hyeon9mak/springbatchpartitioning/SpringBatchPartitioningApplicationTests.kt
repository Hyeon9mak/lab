package com.hyeon9mak.springbatchpartitioning

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(properties = ["spring.batch.job.enabled=false"])
class SpringBatchPartitioningApplicationTests {

    @Test
    fun contextLoads() {
    }

}
