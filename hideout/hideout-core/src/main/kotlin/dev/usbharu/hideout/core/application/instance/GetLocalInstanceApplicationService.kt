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

import dev.usbharu.hideout.core.application.exception.InternalServerException
import dev.usbharu.hideout.core.application.model.Instance
import dev.usbharu.hideout.core.application.shared.AbstractApplicationService
import dev.usbharu.hideout.core.application.shared.Transaction
import dev.usbharu.hideout.core.config.ApplicationConfig
import dev.usbharu.hideout.core.domain.model.instance.InstanceRepository
import dev.usbharu.hideout.core.domain.model.support.principal.Principal
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class GetLocalInstanceApplicationService(
    private val applicationConfig: ApplicationConfig,
    private val instanceRepository: InstanceRepository,
    transaction: Transaction
) :
    AbstractApplicationService<Unit, Instance>(
        transaction,
        logger
    ) {
    private var cachedInstance: Instance? = null

    override suspend fun internalExecute(command: Unit, principal: Principal): Instance {
        if (cachedInstance != null) {
            logger.trace("Use cache {}", cachedInstance)
            @Suppress("UnsafeCallOnNullableType")
            return cachedInstance!!
        }

        val instance = (
            instanceRepository.findByUrl(applicationConfig.url)
                ?: throw InternalServerException("Local instance not found.")
            )

        cachedInstance = Instance.of(instance)
        @Suppress("UnsafeCallOnNullableType")
        return cachedInstance!!
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GetLocalInstanceApplicationService::class.java)
    }
}
