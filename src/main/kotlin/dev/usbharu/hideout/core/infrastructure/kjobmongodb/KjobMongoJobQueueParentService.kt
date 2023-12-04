package dev.usbharu.hideout.core.infrastructure.kjobmongodb

import com.mongodb.reactivestreams.client.MongoClient
import dev.usbharu.hideout.core.external.job.HideoutJob
import dev.usbharu.hideout.core.service.job.JobQueueParentService
import kjob.core.Job
import kjob.core.dsl.ScheduleContext
import kjob.core.kjob
import kjob.mongo.Mongo
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(name = ["hideout.use-mongodb"], havingValue = "true", matchIfMissing = false)
class KjobMongoJobQueueParentService(private val mongoClient: MongoClient) : JobQueueParentService, AutoCloseable {
    private val kjob = kjob(Mongo) {
        client = mongoClient
        databaseName = "kjob"
        jobCollection = "kjob-jobs"
        lockCollection = "kjob-locks"
        expireLockInMinutes = 5L
        isWorker = false
    }.start()

    override fun init(jobDefines: List<Job>) = Unit

    @Deprecated("use type safe → scheduleTypeSafe")
    override suspend fun <J : Job> schedule(job: J, block: ScheduleContext<J>.(J) -> Unit) {
        logger.debug("SCHEDULE Job: {}", job.name)
        kjob.schedule(job, block)
    }

    override suspend fun <T, J : HideoutJob<T, J>> scheduleTypeSafe(job: J, jobProps: T) {
        logger.debug("SCHEDULE Job: {}", job.name)
        logger.trace("Job props: {}", jobProps)
        val convert = job.convert(jobProps)
        kjob.schedule(job, convert)
        logger.debug("SUCCESS Job: {}", job.name)
    }

    override fun close() {
        kjob.shutdown()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(KjobMongoJobQueueParentService::class.java)
    }
}
