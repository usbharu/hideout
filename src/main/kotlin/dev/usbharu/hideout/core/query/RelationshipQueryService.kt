package dev.usbharu.hideout.core.query

import dev.usbharu.hideout.core.domain.model.relationship.Relationship

interface RelationshipQueryService {

    suspend fun findByTargetIdAndFollowing(targetId: Long, following: Boolean): List<Relationship>
    suspend fun findByTargetIdAndFollowRequestAndIgnoreFollowRequest(
        targetId: Long,
        followRequest: Boolean,
        ignoreFollowRequest: Boolean
    ): List<Relationship>
}
