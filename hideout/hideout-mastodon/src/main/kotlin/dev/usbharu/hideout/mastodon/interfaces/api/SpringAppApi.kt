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

package dev.usbharu.hideout.mastodon.interfaces.api

import dev.usbharu.hideout.core.application.application.RegisterApplication
import dev.usbharu.hideout.core.application.application.RegisterApplicationApplicationService
import dev.usbharu.hideout.core.domain.model.support.principal.Anonymous
import dev.usbharu.hideout.mastodon.interfaces.api.generated.AppApi
import dev.usbharu.hideout.mastodon.interfaces.api.generated.model.Application
import dev.usbharu.hideout.mastodon.interfaces.api.generated.model.AppsRequest
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import java.net.URI

@Controller
class SpringAppApi(private val registerApplicationApplicationService: RegisterApplicationApplicationService) : AppApi {
    override suspend fun apiV1AppsPost(appsRequest: AppsRequest): ResponseEntity<Application> {
        val registerApplication = RegisterApplication(
            appsRequest.clientName,
            setOf(URI.create(appsRequest.redirectUris)),
            false,
            appsRequest.scopes?.split(" ").orEmpty().toSet().ifEmpty { setOf("read") }
        )
        val registeredApplication = registerApplicationApplicationService.execute(registerApplication, Anonymous)
        return ResponseEntity.ok(
            Application(
                name = registeredApplication.name,
                vapidKey = "invalid-vapid-key",
                website = null,
                clientId = registeredApplication.clientId,
                clientSecret = registeredApplication.clientSecret,
                redirectUri = appsRequest.redirectUris
            )
        )
    }
}
