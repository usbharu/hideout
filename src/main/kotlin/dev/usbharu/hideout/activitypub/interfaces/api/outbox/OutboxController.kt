package dev.usbharu.hideout.activitypub.interfaces.api.outbox

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
interface OutboxController {
    @RequestMapping("/outbox", "/users/{username}/outbox", method = [RequestMethod.POST, RequestMethod.GET])
    suspend fun outbox(@RequestBody string: String): ResponseEntity<Unit> = ResponseEntity(HttpStatus.ACCEPTED)
}