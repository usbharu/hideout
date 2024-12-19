package dev.usbharu.hideout.core.domain.service.relationship

import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.relationship.Relationship
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RelationshipDomainServiceTest {
    @Test
    fun block_relationshipとinverseRelationshipが同じ場合失敗() {
        val relationship = Relationship.default(ActorId(1), ActorId(2))
        assertThrows<IllegalArgumentException> {
            RelationshipDomainService().block(relationship, relationship)
        }
    }

    @Test
    fun block_relationship_actorIdとinverseRelationshio_targetActorIdが同じ場合失敗() {
        val relationship = Relationship.default(ActorId(1), ActorId(2))
        val inverseRelationship = Relationship.default(ActorId(2), ActorId(2))
        assertThrows<IllegalArgumentException> {
            RelationshipDomainService().block(relationship, inverseRelationship)
        }
    }

    @Test
    fun block_relationship_targetActorIdとinverseRelationship_actorIdが同じ場合失敗() {
        val relationship = Relationship.default(ActorId(1), ActorId(2))
        val inverseRelationship = Relationship.default(ActorId(1), ActorId(1))
        assertThrows<IllegalArgumentException> {
            RelationshipDomainService().block(relationship, inverseRelationship)
        }
    }

    @Test
    fun block_ブロックされお互いのフォローとフォローリクエストが外れる() {
        val relationship = Relationship(
            ActorId(1),
            ActorId(2),
            following = true,
            blocking = false,
            muting = false,
            followRequesting = false,
            mutingFollowRequest = false
        )
        val inverseRelationship = Relationship(
            ActorId(2),
            ActorId(1),
            following = false,
            blocking = false,
            followRequesting = true,
            mutingFollowRequest = false,
            muting = false
        )

        RelationshipDomainService().block(relationship, inverseRelationship)

        assertTrue(relationship.blocking)
        assertFalse(relationship.following)
        assertFalse(relationship.followRequesting)
        assertFalse(inverseRelationship.following)
        assertFalse(inverseRelationship.followRequesting)
    }

    @Test
    fun followRequest_relationshipとinverseRelationshipが同じ場合失敗() {
        val relationship = Relationship.default(ActorId(1), ActorId(2))
        assertThrows<IllegalArgumentException> {
            RelationshipDomainService().followRequest(relationship, relationship)
        }
    }

    @Test
    fun followRequest_relationship_actorIdとinverseRelationshio_targetActorIdが同じ場合失敗() {
        val relationship = Relationship.default(ActorId(1), ActorId(2))
        val inverseRelationship = Relationship.default(ActorId(2), ActorId(2))
        assertThrows<IllegalArgumentException> {
            RelationshipDomainService().followRequest(relationship, inverseRelationship)
        }
    }

    @Test
    fun followRequest_relationship_targetActorIdとinverseRelationship_actorIdが同じ場合失敗() {
        val relationship = Relationship.default(ActorId(1), ActorId(2))
        val inverseRelationship = Relationship.default(ActorId(1), ActorId(1))
        assertThrows<IllegalArgumentException> {
            RelationshipDomainService().followRequest(relationship, inverseRelationship)
        }
    }

    @Test
    fun followRequest_ブロックされてる場合失敗() {
        val relationship = Relationship.default(ActorId(1), ActorId(2))
        val inverseRelationship = Relationship(
            ActorId(2),
            ActorId(1),
            following = false,
            blocking = true,
            muting = false,
            followRequesting = false,
            mutingFollowRequest = false
        )
        assertThrows<IllegalArgumentException> {
            RelationshipDomainService().followRequest(relationship, inverseRelationship)
        }
    }

    @Test
    fun followRequest_ブロックしてる場合は失敗() {
        val relationship = Relationship(
            ActorId(1),
            ActorId(2),
            following = false,
            blocking = true,
            muting = false,
            followRequesting = false,
            mutingFollowRequest = false
        )
        val inverseRelationship = Relationship.default(ActorId(2), ActorId(1))
        assertThrows<IllegalArgumentException> {
            RelationshipDomainService().followRequest(relationship, inverseRelationship)
        }
    }

    @Test
    fun followRequest_followRequestingがtrueになる() {
        val relationship = Relationship.default(ActorId(1), ActorId(2))
        val inverseRelationship = Relationship.default(ActorId(2), ActorId(1))

        RelationshipDomainService().followRequest(relationship, inverseRelationship)

        assertTrue(relationship.followRequesting)
    }
}