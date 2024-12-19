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

package dev.usbharu.hideout.core.application.post

import dev.usbharu.hideout.core.application.exception.InternalServerException
import dev.usbharu.hideout.core.application.model.PostDetail
import dev.usbharu.hideout.core.application.shared.AbstractApplicationService
import dev.usbharu.hideout.core.application.shared.Transaction
import dev.usbharu.hideout.core.domain.model.actor.Actor
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.domain.model.media.Media
import dev.usbharu.hideout.core.domain.model.media.MediaRepository
import dev.usbharu.hideout.core.domain.model.post.PostId
import dev.usbharu.hideout.core.domain.model.post.PostRepository
import dev.usbharu.hideout.core.domain.model.reaction.ReactionRepository
import dev.usbharu.hideout.core.domain.model.support.principal.Principal
import dev.usbharu.hideout.core.domain.service.post.IPostReadAccessControl
import dev.usbharu.hideout.core.query.reactions.ReactionsQueryService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class GetPostDetailApplicationService(
    transaction: Transaction,
    private val postRepository: PostRepository,
    private val actorRepository: ActorRepository,
    private val mediaRepository: MediaRepository,
    private val iPostReadAccessControl: IPostReadAccessControl,
    private val reactionsQueryService: ReactionsQueryService,
    private val reactionRepository: ReactionRepository,
) : AbstractApplicationService<GetPostDetail, PostDetail>(
    transaction,
    logger
) {
    override suspend fun internalExecute(command: GetPostDetail, principal: Principal): PostDetail {
        val post = postRepository.findById(PostId(command.postId))
            ?: throw IllegalArgumentException("Post ${command.postId} not found.")
        if (iPostReadAccessControl.isAllow(post, principal).not()) {
            throw IllegalArgumentException("Post ${command.postId} not found.")
        }
        val actor =
            actorRepository.findById(post.actorId) ?: throw InternalServerException("Actor ${post.actorId} not found.")

        val iconMedia = actor.icon?.let { mediaRepository.findById(it) }

        val mediaList = mediaRepository.findByIdIn(post.mediaIds)

        val reactions = reactionsQueryService.findAllByPostId(post.id)

        val favourited = reactionRepository.existsByPostIdAndActorIdAndCustomEmojiIdOrUnicodeEmoji(
            post.id,
            principal.actorId,
            null,
            "❤"
        )

        return PostDetail.of(
            post = post,
            actor = actor,
            iconMedia = iconMedia,
            mediaList = mediaList,
            reply = post.replyId?.let { fetchChild(it, actor, iconMedia, principal) },
            repost = post.repostId?.let { fetchChild(it, actor, iconMedia, principal) },
            moveTo = post.moveTo?.let { fetchChild(it, actor, iconMedia, principal) },
            reactionsList = reactions,
            favourited
        )
    }

    private suspend fun fetchChild(
        postId: PostId,
        actor: Actor,
        iconMedia: Media?,
        principal: Principal
    ): PostDetail? {
        val post = postRepository.findById(postId) ?: return null

        if (iPostReadAccessControl.isAllow(post, principal).not()) {
            return null
        }

        val (first, third) = if (actor.id != post.actorId) {
            (actorRepository.findById(post.actorId) ?: return null) to actor.icon?.let { mediaRepository.findById(it) }
        } else {
            actor to iconMedia
        }

        val mediaList = mediaRepository.findByIdIn(post.mediaIds)
        return PostDetail.of(
            post = post,
            actor = first,
            iconMedia = third,
            mediaList = mediaList,
            reactionsList = emptyList(),
            favourited = false
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GetPostDetailApplicationService::class.java)
    }
}

data class GetPostDetail(val postId: Long)
