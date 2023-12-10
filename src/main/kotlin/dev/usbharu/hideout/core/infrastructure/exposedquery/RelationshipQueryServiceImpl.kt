package dev.usbharu.hideout.core.infrastructure.exposedquery

import dev.usbharu.hideout.core.domain.model.relationship.Relationship
import dev.usbharu.hideout.core.domain.model.relationship.Relationships
import dev.usbharu.hideout.core.domain.model.relationship.toRelationships
import dev.usbharu.hideout.core.query.RelationshipQueryService
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.springframework.stereotype.Service

@Service
class RelationshipQueryServiceImpl : RelationshipQueryService {
    override suspend fun findByTargetIdAndFollowing(targetId: Long, following: Boolean): List<Relationship> =
        Relationships.select { Relationships.targetUserId eq targetId and (Relationships.following eq following) }
            .map { it.toRelationships() }
}