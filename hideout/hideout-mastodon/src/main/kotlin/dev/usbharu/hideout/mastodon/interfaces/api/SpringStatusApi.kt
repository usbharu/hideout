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

import dev.usbharu.hideout.core.application.post.RegisterLocalPost
import dev.usbharu.hideout.core.application.post.RegisterLocalPostApplicationService
import dev.usbharu.hideout.core.application.reaction.CreateReaction
import dev.usbharu.hideout.core.application.reaction.RemoveReaction
import dev.usbharu.hideout.core.application.reaction.UserCreateReactionApplicationService
import dev.usbharu.hideout.core.application.reaction.UserRemoveReactionApplicationService
import dev.usbharu.hideout.core.domain.model.post.Visibility
import dev.usbharu.hideout.core.infrastructure.springframework.oauth2.SpringSecurityOauth2PrincipalContextHolder
import dev.usbharu.hideout.mastodon.application.status.GetStatus
import dev.usbharu.hideout.mastodon.application.status.GetStatusApplicationService
import dev.usbharu.hideout.mastodon.interfaces.api.StatusesRequest.Visibility.*
import dev.usbharu.hideout.mastodon.interfaces.api.generated.StatusApi
import dev.usbharu.hideout.mastodon.interfaces.api.generated.model.Status
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller

@Controller
class SpringStatusApi(
    private val registerLocalPostApplicationService: RegisterLocalPostApplicationService,
    private val getStatusApplicationService: GetStatusApplicationService,
    private val principalContextHolder: SpringSecurityOauth2PrincipalContextHolder,
    private val userCreateReactionApplicationService: UserCreateReactionApplicationService,
    private val userRemoveReactionApplicationService: UserRemoveReactionApplicationService
) : StatusApi {
    override suspend fun apiV1StatusesIdEmojiReactionsEmojiDelete(id: String, emoji: String): ResponseEntity<Status> =
        super.apiV1StatusesIdEmojiReactionsEmojiDelete(id, emoji)

    override suspend fun apiV1StatusesIdEmojiReactionsEmojiPut(id: String, emoji: String): ResponseEntity<Status> =
        super.apiV1StatusesIdEmojiReactionsEmojiPut(id, emoji)

    override suspend fun apiV1StatusesIdGet(id: String): ResponseEntity<Status> {
        return ResponseEntity.ok(
            getStatusApplicationService.execute(
                GetStatus(id),
                principalContextHolder.getPrincipal()
            )
        )
    }

    override suspend fun apiV1StatusesIdFavouritePost(id: String): ResponseEntity<Status> {
        val principal = principalContextHolder.getPrincipal()

        userCreateReactionApplicationService.execute(CreateReaction(postId = id.toLong(), null, "❤"), principal)
        return ResponseEntity.ok(getStatusApplicationService.execute(GetStatus(id), principal))
    }

    override suspend fun apiV1StatusesIdUnfavouritePost(id: String): ResponseEntity<Status> {
        val principal = principalContextHolder.getPrincipal()

        userRemoveReactionApplicationService.execute(RemoveReaction(postId = id.toLong(), null, "❤"), principal)
        return ResponseEntity.ok(getStatusApplicationService.execute(GetStatus(id), principal))
    }

    override suspend fun apiV1StatusesPost(statusesRequest: StatusesRequest): ResponseEntity<Status> {
        val principal = principalContextHolder.getPrincipal()
        val execute = registerLocalPostApplicationService.execute(
            RegisterLocalPost(
                content = statusesRequest.status.orEmpty(),
                overview = statusesRequest.spoiler_text,
                visibility = when (statusesRequest.visibility) {
                    public -> Visibility.PUBLIC
                    unlisted -> Visibility.UNLISTED
                    private -> Visibility.FOLLOWERS
                    direct -> Visibility.DIRECT
                    null -> Visibility.PUBLIC
                },
                repostId = null,
                replyId = statusesRequest.in_reply_to_id?.toLong(),
                sensitive = statusesRequest.sensitive == true,
                mediaIds = statusesRequest.media_ids.map { it.toLong() }
            ),
            principal
        )

        val status =
            getStatusApplicationService.execute(GetStatus(execute.toString()), principal)
        return ResponseEntity.ok(
            status
        )
    }
}
