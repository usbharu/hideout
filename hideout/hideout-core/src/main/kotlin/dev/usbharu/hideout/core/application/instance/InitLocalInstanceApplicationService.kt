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

package dev.usbharu.hideout.core.application.instance

import dev.usbharu.hideout.core.application.shared.Transaction
import dev.usbharu.hideout.core.config.ApplicationConfig
import dev.usbharu.hideout.core.domain.model.instance.*
import dev.usbharu.hideout.core.domain.model.support.domain.apHost
import dev.usbharu.hideout.core.domain.shared.id.IdGenerateService
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.info.BuildProperties
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class InitLocalInstanceApplicationService(
    private val applicationConfig: ApplicationConfig,
    private val instanceRepository: InstanceRepository,
    private val idGenerateService: IdGenerateService,
    private val buildProperties: BuildProperties,
    private val transaction: Transaction,
) {
    @EventListener(ApplicationReadyEvent::class)
    suspend fun init() = transaction.transaction {
        val findByUrl = instanceRepository.findByUrl(applicationConfig.url)

        if (findByUrl == null) {
            val instance = Instance(
                id = InstanceId(idGenerateService.generateId()),
                name = InstanceName(applicationConfig.url.apHost),
                description = InstanceDescription(""),
                url = applicationConfig.url,
                iconUrl = applicationConfig.url,
                sharedInbox = null,
                software = InstanceSoftware("hideout"),
                version = InstanceVersion(buildProperties.version),
                isBlocked = false,
                isMuted = false,
                moderationNote = InstanceModerationNote(""),
                createdAt = Instant.now(),
            )
            instanceRepository.save(instance)
        }
    }
}
