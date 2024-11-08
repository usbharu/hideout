package dev.usbharu.hideout.activitypub.`interface`.api

import dev.usbharu.hideout.activitypub.application.actor.GetActorApplicationService
import dev.usbharu.hideout.core.domain.model.actor.Actor
import dev.usbharu.hideout.core.domain.model.support.principal.Anonymous
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@Controller
class APActorController(private val getActorApplicationService: GetActorApplicationService) {
    @GetMapping(
        "/users/{username}",
        consumes = ["application/activity+json"],
        produces = ["application/activity+json"]
    )
    suspend fun user(@PathVariable username: String): Actor {
        return getActorApplicationService.execute(username, Anonymous)
    }
}