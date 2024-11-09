package dev.usbharu.hideout.activitypub.infrastructure.owl

import dev.usbharu.hideout.activitypub.domain.shared.jobqueue.TaskPublisher
import dev.usbharu.hideout.activitypub.domain.task.Task
import dev.usbharu.owl.producer.api.OwlProducer
import org.springframework.stereotype.Service

@Service
class OwlTaskPublisher(private val owlProducer: OwlProducer) : TaskPublisher {
    override suspend fun publish(task: Task<*>) {
        owlProducer.publishTask(task)
    }
}