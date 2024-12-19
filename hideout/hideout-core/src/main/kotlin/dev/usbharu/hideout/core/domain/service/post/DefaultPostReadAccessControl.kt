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

package dev.usbharu.hideout.core.domain.service.post

import dev.usbharu.hideout.core.domain.model.post.Post
import dev.usbharu.hideout.core.domain.model.post.Visibility
import dev.usbharu.hideout.core.domain.model.relationship.Relationship
import dev.usbharu.hideout.core.domain.model.relationship.RelationshipRepository
import dev.usbharu.hideout.core.domain.model.support.principal.Anonymous
import dev.usbharu.hideout.core.domain.model.support.principal.Principal
import org.springframework.stereotype.Component

interface IPostReadAccessControl {
    suspend fun isAllow(post: Post, principal: Principal): Boolean
    suspend fun areAllows(postList: List<Post>, principal: Principal): List<Post>
}

@Component
class DefaultPostReadAccessControl(private val relationshipRepository: RelationshipRepository) :
    IPostReadAccessControl {
    override suspend fun isAllow(post: Post, principal: Principal): Boolean {
        // ポスト主は無条件で見れる
        if (post.actorId == principal.actorId) {
            return true
        }

        val relationship = (
            relationshipRepository.findByActorIdAndTargetId(post.actorId, principal.actorId)
                ?: Relationship.default(post.actorId, principal.actorId)
            )

        // ブロックされてたら見れない
        if (relationship.blocking) {
            return false
        }

        // PublicかUnlistedなら見れる
        if (post.visibility == Visibility.PUBLIC || post.visibility == Visibility.UNLISTED) {
            return true
        }

        // principalがAnonymousなら見れない
        if (principal is Anonymous) {
            return false
        }

        // DirectでvisibleActorsに含まれていたら見れる
        if (post.visibility == Visibility.DIRECT && post.visibleActors.contains(principal.actorId)) {
            return true
        }

        // Followersでフォロワーなら見れる
        if (post.visibility == Visibility.FOLLOWERS) {
            val inverseRelationship =
                relationshipRepository.findByActorIdAndTargetId(principal.actorId, post.actorId) ?: return false

            return inverseRelationship.following
        }

        // その他の場合は見れない
        return false
    }

    override suspend fun areAllows(postList: List<Post>, principal: Principal): List<Post> {
        val actorIds = postList.map { it.actorId }
        val blockedByList =
            relationshipRepository.findByActorIdsAndTargetIdAndBlocking(actorIds, principal.actorId, true)
                .map { it.actorId }
        val followingList =
            relationshipRepository.findByActorIdAndTargetIdsAndFollowing(principal.actorId, actorIds, true)
                .map { it.targetActorId }

        fun internalAllow(post: Post): Boolean {
            // ポスト主は無条件で見れる
            if (post.actorId == principal.actorId) {
                return true
            }

            if (blockedByList.contains(post.actorId)) {
                return false
            }

            if (post.visibility == Visibility.PUBLIC || post.visibility == Visibility.UNLISTED) {
                return true
            }

            if (principal is Anonymous) {
                return false
            }

            if (post.visibility == Visibility.DIRECT && post.visibleActors.contains(principal.actorId)) {
                return true
            }

            if (post.visibility == Visibility.FOLLOWERS && followingList.contains(post.actorId)) {
                return true
            }
            return false
        }

        return postList
            .filter {
                internalAllow(it)
            }
    }
}
