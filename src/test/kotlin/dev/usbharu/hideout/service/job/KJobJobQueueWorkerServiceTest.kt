package dev.usbharu.hideout.service.job

import kjob.core.Job
import org.jetbrains.exposed.sql.Database
import org.junit.jupiter.api.Test

class KJobJobQueueWorkerServiceTest {

    object TestJob : Job("test-job")

    @Test
    fun init() {
        val kJobJobWorkerService = KJobJobQueueWorkerService(Database.connect("jdbc:h2:mem:"))
        kJobJobWorkerService.init(listOf(TestJob to { it -> execute { it as TestJob;println(it.propNames) } }))
    }
}
