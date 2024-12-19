package dev.usbharu.hideout.activitypub.domain.shared.jobqueue

import dev.usbharu.hideout.activitypub.domain.task.Task

interface TaskPublisher {
    suspend fun publish(task: Task<*>)
}