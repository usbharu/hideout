package dev.usbharu.hideout.activitypub.domain.task

import dev.usbharu.hideout.core.domain.model.support.domain.Domain
import dev.usbharu.owl.common.task.Task
import java.time.Instant

class Task<out T : TaskBody>(
    val id: String,
    val name: String,
    val publishedOn: Instant,
    val body: T,
    val domain: Domain
) : Task() {

}