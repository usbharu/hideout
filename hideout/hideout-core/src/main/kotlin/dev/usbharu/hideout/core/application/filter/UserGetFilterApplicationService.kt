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

package dev.usbharu.hideout.core.application.filter

import dev.usbharu.hideout.core.application.exception.PermissionDeniedException
import dev.usbharu.hideout.core.application.model.Filter
import dev.usbharu.hideout.core.application.shared.LocalUserAbstractApplicationService
import dev.usbharu.hideout.core.application.shared.Transaction
import dev.usbharu.hideout.core.domain.model.filter.FilterId
import dev.usbharu.hideout.core.domain.model.filter.FilterRepository
import dev.usbharu.hideout.core.domain.model.support.principal.LocalUser
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class UserGetFilterApplicationService(private val filterRepository: FilterRepository, transaction: Transaction) :
    LocalUserAbstractApplicationService<GetFilter, Filter>(
        transaction,
        logger
    ) {
    override suspend fun internalExecute(command: GetFilter, principal: LocalUser): Filter {
        val filter =
            filterRepository.findByFilterId(FilterId(command.filterId))
                ?: throw IllegalArgumentException("Filter ${command.filterId} not found.")
        if (filter.userDetailId != principal.userDetailId) {
            throw PermissionDeniedException()
        }
        return Filter.of(filter)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(UserGetFilterApplicationService::class.java)
    }
}

data class GetFilter(val filterId: Long)
