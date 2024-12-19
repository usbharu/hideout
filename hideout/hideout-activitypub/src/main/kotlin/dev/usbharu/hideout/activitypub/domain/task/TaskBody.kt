package dev.usbharu.hideout.activitypub.domain.task

import dev.usbharu.hideout.core.domain.model.support.principal.Principal

abstract class TaskBody(private val map: Map<String, Any?>, val principal: Principal) {
    fun toMap(): Map<String, Any?> = map
}