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

import dev.usbharu.hideout.core.application.model.PostDetail
import dev.usbharu.hideout.core.application.shared.AbstractApplicationService
import dev.usbharu.hideout.core.application.shared.Transaction
import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.post.PostId
import dev.usbharu.hideout.core.domain.model.post.PostRepository
import dev.usbharu.hideout.core.domain.model.post.Visibility
import dev.usbharu.hideout.core.domain.model.support.page.Page
import dev.usbharu.hideout.core.domain.model.support.page.PaginationList
import dev.usbharu.hideout.core.domain.model.support.principal.Principal
import dev.usbharu.hideout.core.query.usertimeline.UserTimelineQueryService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class GetUserTimelineApplicationService(
    private val userTimelineQueryService: UserTimelineQueryService,
    private val postRepository: PostRepository,
    transaction: Transaction
) :
    AbstractApplicationService<GetUserTimeline, PaginationList<PostDetail, PostId>>(transaction, logger) {
    override suspend fun internalExecute(
        command: GetUserTimeline,
        principal: Principal
    ): PaginationList<PostDetail, PostId> {
        val postList = postRepository.findByActorIdAndVisibilityInList(
            ActorId(command.id),
            listOf(Visibility.PUBLIC, Visibility.UNLISTED, Visibility.FOLLOWERS),
            command.page
        )

        val postIdList =
            postList.mapNotNull { it.repostId } + postList.mapNotNull { it.replyId } + postList.map { it.id }

        val postDetailMap = userTimelineQueryService.findByIdAll(postIdList, principal).associateBy { it.id }

        return PaginationList(
            postList.mapNotNull {
                postDetailMap[it.id.id]?.copy(
                    repost = postDetailMap[it.repostId?.id],
                    reply = postDetailMap[it.replyId?.id]
                )
            },
            postList.next,
            postList.prev
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GetUserTimelineApplicationService::class.java)
    }
}

data class GetUserTimeline(val id: Long, val page: Page)
