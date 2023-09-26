package dev.usbharu.hideout

import dev.usbharu.hideout.domain.model.job.HideoutJob
import dev.usbharu.hideout.service.ap.APService
import dev.usbharu.hideout.service.job.JobQueueParentService
import dev.usbharu.hideout.service.job.JobQueueWorkerService
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class JobQueueRunner(private val jobQueueParentService: JobQueueParentService, private val jobs: List<HideoutJob>) :
    ApplicationRunner {
    override fun run(args: ApplicationArguments?) {
        LOGGER.info("Init job queue. ${jobs.size}")
        jobQueueParentService.init(jobs)
    }

    companion object {
        val LOGGER = LoggerFactory.getLogger(JobQueueRunner::class.java)
    }
}

@Component
class JobQueueWorkerRunner(
    private val jobQueueWorkerService: JobQueueWorkerService,
    private val jobs: List<HideoutJob>,
    private val apService: APService
) : ApplicationRunner {
    override fun run(args: ApplicationArguments?) {
        LOGGER.info("Init job queue worker.")
        jobQueueWorkerService.init(
            jobs.map {
                it to {
                    execute {
                        LOGGER.debug("excute job ${it.name}")
                        apService.processActivity(
                            job = this,
                            hideoutJob = it
                        )
                    }
                }
            }
        )
    }

    companion object {
        val LOGGER = LoggerFactory.getLogger(JobQueueWorkerRunner::class.java)
    }
}