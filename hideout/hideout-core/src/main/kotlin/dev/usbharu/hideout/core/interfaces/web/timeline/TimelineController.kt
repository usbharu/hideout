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

package dev.usbharu.hideout.core.interfaces.web.timeline

import dev.usbharu.hideout.core.application.exception.InternalServerException
import dev.usbharu.hideout.core.application.shared.Transaction
import dev.usbharu.hideout.core.application.timeline.ReadTimeline
import dev.usbharu.hideout.core.application.timeline.ReadTimelineApplicationService
import dev.usbharu.hideout.core.domain.model.support.page.Page
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailRepository
import dev.usbharu.hideout.core.infrastructure.springframework.SpringSecurityFormLoginPrincipalContextHolder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class TimelineController(
    private val readTimelineApplicationService: ReadTimelineApplicationService,
    private val userDetailRepository: UserDetailRepository,
    private val springSecurityFormLoginPrincipalContextHolder: SpringSecurityFormLoginPrincipalContextHolder,
    private val transaction: Transaction
) {
    @GetMapping("/home")
    suspend fun homeTimeline(
        model: Model,
        @RequestParam("since_id") sinceId: String?,
        @RequestParam("max_id") maxId: String?,
        @RequestParam("min_id") minId: String?
    ): String {
        val principal = springSecurityFormLoginPrincipalContextHolder.getPrincipal()
        val userDetail = transaction.transaction {
            userDetailRepository.findByActorId(principal.actorId.id)
                ?: throw InternalServerException("UserDetail not found.")
        }

        val homeTimelineId =
            userDetail.homeTimelineId ?: throw InternalServerException("HomeTimeline ${userDetail.id} is null.")
        val execute = readTimelineApplicationService.execute(
            ReadTimeline(
                timelineId = homeTimelineId.value,
                mediaOnly = false,
                localOnly = false,
                remoteOnly = false,
                page = Page.of(
                    maxId = maxId?.toLongOrNull(),
                    sinceId = sinceId?.toLongOrNull(),
                    minId = minId?.toLongOrNull(),
                    limit = 20
                )
            ),
            principal
        )

        model.addAttribute("timeline", execute)

        return "homeTimeline"
    }
}
