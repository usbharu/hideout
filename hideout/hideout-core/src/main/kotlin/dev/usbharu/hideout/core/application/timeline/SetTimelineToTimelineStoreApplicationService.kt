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

package dev.usbharu.hideout.core.application.timeline

import dev.usbharu.hideout.core.application.shared.AbstractApplicationService
import dev.usbharu.hideout.core.application.shared.Transaction
import dev.usbharu.hideout.core.domain.model.support.principal.Principal
import dev.usbharu.hideout.core.domain.model.timeline.TimelineId
import dev.usbharu.hideout.core.domain.model.timeline.TimelineRepository
import dev.usbharu.hideout.core.external.timeline.TimelineStore
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class SetTimelineToTimelineStoreApplicationService(
    transaction: Transaction,
    private val timelineStore: TimelineStore,
    private val timelineRepository: TimelineRepository
) :
    AbstractApplicationService<SetTimleineStore, Unit>(
        transaction,
        logger
    ) {
    override suspend fun internalExecute(command: SetTimleineStore, principal: Principal) {
        val findById = timelineRepository.findById(command.timelineId)
            ?: throw IllegalArgumentException("Timeline ${command.timelineId} not found")
        timelineStore.addTimeline(findById, emptyList())
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SetTimelineToTimelineStoreApplicationService::class.java)
    }
}

data class SetTimleineStore(val timelineId: TimelineId)
