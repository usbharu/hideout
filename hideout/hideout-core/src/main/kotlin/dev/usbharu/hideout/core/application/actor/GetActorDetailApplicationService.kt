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

package dev.usbharu.hideout.core.application.actor

import dev.usbharu.hideout.core.application.model.ActorDetail
import dev.usbharu.hideout.core.application.shared.AbstractApplicationService
import dev.usbharu.hideout.core.application.shared.Transaction
import dev.usbharu.hideout.core.config.ApplicationConfig
import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.domain.model.media.MediaRepository
import dev.usbharu.hideout.core.domain.model.support.acct.Acct
import dev.usbharu.hideout.core.domain.model.support.domain.apHost
import dev.usbharu.hideout.core.domain.model.support.principal.Principal
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class GetActorDetailApplicationService(
    private val actorRepository: ActorRepository,
    private val mediaRepository: MediaRepository,
    private val applicationConfig: ApplicationConfig,
    transaction: Transaction
) :
    AbstractApplicationService<GetActorDetail, ActorDetail>(
        transaction,
        logger
    ) {
    override suspend fun internalExecute(command: GetActorDetail, principal: Principal): ActorDetail {
        val actor = if (command.id != null) {
            actorRepository.findById(ActorId(command.id))
                ?: throw IllegalArgumentException("Actor ${command.id} not found.")
        } else if (command.actorName != null) {
            val host = if (command.actorName.host.isEmpty()) {
                applicationConfig.url.apHost
            } else {
                command.actorName.host
            }
            actorRepository.findByNameAndDomain(command.actorName.userpart, host)
                ?: throw IllegalArgumentException("Actor ${command.actorName} not found.")
        } else {
            throw IllegalArgumentException("id and actorName are null.")
        }

        val iconUrl = actor.icon?.let { mediaRepository.findById(it) }
        val bannerUrl = actor.banner?.let { mediaRepository.findById(it) }

        return ActorDetail.of(actor, iconUrl, bannerUrl)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GetActorDetailApplicationService::class.java)
    }
}

data class GetActorDetail(
    val actorName: Acct? = null,
    val id: Long? = null
)
