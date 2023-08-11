package dev.usbharu.hideout.plugins

import dev.usbharu.hideout.exception.InvalidUsernameOrPasswordException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<IllegalArgumentException> { call, cause ->
            call.respondText(text = "400: $cause", status = HttpStatusCode.BadRequest)
        }
        exception<InvalidUsernameOrPasswordException> { call, _ ->
            call.respond(401)
        }
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: ${cause.stackTraceToString()}", status = HttpStatusCode.InternalServerError)
            cause.printStackTrace()
        }
    }
}
