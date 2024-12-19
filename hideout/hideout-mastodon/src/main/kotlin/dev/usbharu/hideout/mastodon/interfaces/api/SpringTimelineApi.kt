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

import dev.usbharu.hideout.core.application.exception.InternalServerException
import dev.usbharu.hideout.core.application.shared.Transaction
import dev.usbharu.hideout.core.domain.model.support.page.Page
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailRepository
import dev.usbharu.hideout.core.infrastructure.springframework.oauth2.SpringSecurityOauth2PrincipalContextHolder
import dev.usbharu.hideout.mastodon.application.timeline.MastodonReadTimeline
import dev.usbharu.hideout.mastodon.application.timeline.MastodonReadTimelineApplicationService
import dev.usbharu.hideout.mastodon.interfaces.api.generated.TimelineApi
import dev.usbharu.hideout.mastodon.interfaces.api.generated.model.Status
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.slf4j.MDCContext
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller

@Controller
class SpringTimelineApi(
    private val mastodonReadTimelineApplicationService: MastodonReadTimelineApplicationService,

    private val principalContextHolder: SpringSecurityOauth2PrincipalContextHolder,
    private val userDetailRepository: UserDetailRepository,
    private val transaction: Transaction,
) : TimelineApi {
    override fun apiV1TimelinesHomeGet(
        maxId: String?,
        sinceId: String?,
        minId: String?,
        limit: Int?
    ): ResponseEntity<Flow<Status>> = runBlocking(MDCContext()) {
        val principal = principalContextHolder.getPrincipal()
        val userDetail = transaction.transaction {
            userDetailRepository.findByActorId(principal.actorId.id)
                ?: throw InternalServerException("UserDetail not found.")
        }

        val homeTimelineId =
            userDetail.homeTimelineId ?: throw InternalServerException("HomeTimeline ${userDetail.id} is null.")

        ResponseEntity.ok(
            mastodonReadTimelineApplicationService.execute(
                MastodonReadTimeline(
                    timelineId = homeTimelineId.value,
                    mediaOnly = false,
                    localOnly = false,
                    remoteOnly = false,
                    page = Page.of(
                        maxId?.toLongOrNull(),
                        sinceId?.toLongOrNull(),
                        minId?.toLongOrNull(),
                        limit
                    )
                ),
                principal
            ).asFlow()
        )
    }
}
