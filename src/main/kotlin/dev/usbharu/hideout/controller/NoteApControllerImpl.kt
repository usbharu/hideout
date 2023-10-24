package dev.usbharu.hideout.controller

import dev.usbharu.hideout.domain.model.ap.Note
import dev.usbharu.hideout.service.api.NoteApApiService
import dev.usbharu.hideout.service.signature.HttpSignatureUser
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class NoteApControllerImpl(private val noteApApiService: NoteApApiService) : NoteApController {
    override suspend fun postsAp(
        @PathVariable(value = "postId") postId: Long,
        @CurrentSecurityContext context: SecurityContext
    ): ResponseEntity<Note> {
        val userId =
            if (context.authentication is PreAuthenticatedAuthenticationToken &&
                context.authentication.details is HttpSignatureUser
            ) {
                (context.authentication.details as HttpSignatureUser).id
            } else {
                null
            }

        val note = noteApApiService.getNote(postId, userId)
        if (note != null) {
            return ResponseEntity.ok(note)
        }
        return ResponseEntity.notFound().build()
    }
}