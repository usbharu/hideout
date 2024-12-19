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

package dev.usbharu.hideout.mastodon.application.status

import dev.usbharu.hideout.core.application.shared.AbstractApplicationService
import dev.usbharu.hideout.core.application.shared.Transaction
import dev.usbharu.hideout.core.domain.model.support.principal.Principal
import dev.usbharu.hideout.mastodon.interfaces.api.generated.model.Status
import dev.usbharu.hideout.mastodon.query.StatusQueryService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class GetStatusApplicationService(
    private val statusQueryService: StatusQueryService,
    transaction: Transaction,
) : AbstractApplicationService<GetStatus, Status>(
    transaction,
    logger
) {
    override suspend fun internalExecute(command: GetStatus, principal: Principal): Status {
        return statusQueryService.findByPostId(command.id.toLong(), principal)
            ?: throw IllegalArgumentException("Post ${command.id} not found.")
    }

    companion object {
        val logger = LoggerFactory.getLogger(GetStatusApplicationService::class.java)!!
    }
}
