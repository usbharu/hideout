/*
 * Copyright (C) 2024 usbharu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.usbharu.hideout.core.infrastructure.springframework

import dev.usbharu.hideout.core.infrastructure.springframework.oauth2.HideoutUserDetails
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component
import org.springframework.web.servlet.AsyncHandlerInterceptor
import java.util.*

@Component
class ApplicationRequestLogInterceptor : AsyncHandlerInterceptor {
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        MDC.put(requestId, UUID.randomUUID().toString())
        MDC.put(Companion.handler, handler.toString())
        val userDetailId = when (val principal = SecurityContextHolder.getContext().authentication?.principal) {
            is HideoutUserDetails -> {
                principal.userDetailsId
            }

            is Jwt -> {
                principal.getClaim<String>("uid")?.toLongOrNull()
            }

            else -> {
                null
            }
        }

        if (userDetailId != null) {
            MDC.put(userId, userDetailId.toString())
        }

        logger.info("START")
        return true
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        removeMdc()
    }

    override fun afterConcurrentHandlingStarted(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ) {
        removeMdc()
    }

    private fun removeMdc() {
        MDC.remove(requestId)
        MDC.remove(userId)
        MDC.remove(handler)
    }

    companion object {
        const val requestId: String = "requestId"
        const val userId: String = "userId"
        const val handler: String = "handler"
        private val logger = LoggerFactory.getLogger(ApplicationRequestLogInterceptor::class.java)
    }
}
