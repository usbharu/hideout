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

import dev.usbharu.hideout.core.application.exception.InternalServerException
import dev.usbharu.hideout.core.application.shared.AbstractApplicationService
import dev.usbharu.hideout.core.application.shared.Transaction
import dev.usbharu.hideout.core.config.ApplicationConfig
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.domain.model.instance.InstanceRepository
import dev.usbharu.hideout.core.domain.model.support.principal.Principal
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetail
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailId
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailRepository
import dev.usbharu.hideout.core.domain.service.actor.local.LocalActorDomainService
import dev.usbharu.hideout.core.domain.service.userdetail.UserDetailDomainService
import dev.usbharu.hideout.core.domain.shared.id.IdGenerateService
import dev.usbharu.hideout.core.infrastructure.factory.ActorFactoryImpl
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.net.URI

@Service
class RegisterLocalActorApplicationService(
    transaction: Transaction,
    private val actorDomainService: LocalActorDomainService,
    private val actorRepository: ActorRepository,
    private val actorFactoryImpl: ActorFactoryImpl,
    private val instanceRepository: InstanceRepository,
    private val applicationConfig: ApplicationConfig,
    private val userDetailDomainService: UserDetailDomainService,
    private val userDetailRepository: UserDetailRepository,
    private val idGenerateService: IdGenerateService,
) : AbstractApplicationService<RegisterLocalActor, URI>(transaction, Companion.logger) {

    override suspend fun internalExecute(command: RegisterLocalActor, principal: Principal): URI {
        if (actorDomainService.usernameAlreadyUse(command.name)) {
            throw IllegalArgumentException("Username already exists")
        }
        val instance = instanceRepository.findByUrl(applicationConfig.url)
            ?: throw InternalServerException("Local instance not found.")

        val actor = actorFactoryImpl.createLocal(
            command.name,
            actorDomainService.generateKeyPair(),
            instance.id
        )
        actorRepository.save(actor)
        val userDetail = UserDetail.create(
            id = UserDetailId(idGenerateService.generateId()),
            actorId = actor.id,
            password = userDetailDomainService.hashPassword(command.password),
            autoAcceptFolloweeFollowRequest = false,
            lastMigration = null,
            homeTimelineId = null
        )
        userDetailRepository.save(userDetail)
        return actor.url
    }

    companion object {
        private val logger = LoggerFactory.getLogger(RegisterLocalActorApplicationService::class.java)
    }
}

data class RegisterLocalActor(
    val name: String,
    val password: String,
)
