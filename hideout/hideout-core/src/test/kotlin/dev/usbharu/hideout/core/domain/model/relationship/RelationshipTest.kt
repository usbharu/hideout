package dev.usbharu.hideout.core.domain.model.relationship

import dev.usbharu.hideout.core.domain.event.relationship.RelationshipEvent
import dev.usbharu.hideout.core.domain.model.actor.ActorId
import org.junit.jupiter.api.Test
import utils.AssertDomainEvent.assertContainsEvent
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RelationshipTest {
    @Test
    fun unfollow_フォローとフォローリクエストが取り消されUNFOLLOWとUNFOLLOW_REQUESTが発生する() {
        val relationship = Relationship(
            actorId = ActorId(1),
            targetActorId = ActorId(2),
            following = true,
            blocking = false,
            muting = false,
            followRequesting = false,
            mutingFollowRequest = false
        )

        relationship.unfollow()

        assertFalse(relationship.following)
        assertFalse(relationship.followRequesting)
        assertContainsEvent(relationship, RelationshipEvent.UNFOLLOW.eventName)
        assertContainsEvent(relationship, RelationshipEvent.UNFOLLOW_REQUEST.eventName)
    }

    @Test
    fun mute_MUTEが発生する() {
        val relationship = Relationship(
            actorId = ActorId(1),
            targetActorId = ActorId(2),
            following = true,
            blocking = false,
            muting = false,
            followRequesting = false,
            mutingFollowRequest = false
        )

        relationship.mute()

        assertTrue(relationship.muting)
        assertContainsEvent(relationship, RelationshipEvent.MUTE.eventName)
    }

    @Test
    fun unmute_UNMUTEが発生する() {
        val relationship = Relationship(
            actorId = ActorId(1),
            targetActorId = ActorId(2),
            following = true,
            blocking = false,
            muting = true,
            followRequesting = false,
            mutingFollowRequest = true
        )

        relationship.unmute()

        assertFalse(relationship.muting)
        assertContainsEvent(relationship, RelationshipEvent.UNMUTE.eventName)
    }

    @Test
    fun muteFollowRequest_muteFollowiRequestがtrueになる() {
        val relationship = Relationship(
            actorId = ActorId(1),
            targetActorId = ActorId(2),
            following = true,
            blocking = false,
            muting = false,
            followRequesting = true,
            mutingFollowRequest = false
        )

        relationship.muteFollowRequest()

        assertTrue(relationship.mutingFollowRequest)
    }

    @Test
    fun unmuteFollowRequest_muteFollowiRequestがfalseになる() {
        val relationship = Relationship(
            actorId = ActorId(1),
            targetActorId = ActorId(2),
            following = true,
            blocking = false,
            muting = false,
            followRequesting = true,
            mutingFollowRequest = true
        )

        relationship.unmuteFollowRequest()

        assertFalse(relationship.mutingFollowRequest)
    }

    @Test
    fun unfollowRequest_followRequestingがfalseになりUNFOLLOW_REQUESTが発生する() {
        val relationship = Relationship(
            ActorId(1),
            targetActorId = ActorId(2),
            following = false,
            blocking = false,
            muting = false,
            followRequesting = true,
            mutingFollowRequest = false
        )

        relationship.unfollowRequest()

        assertFalse(relationship.followRequesting)
        assertContainsEvent(relationship, RelationshipEvent.UNFOLLOW_REQUEST.eventName)
    }

    @Test
    fun acceptFollowRequest_followingがtrueにfollowRequestingがfalseになりaccept_followが発生する() {
        val relationship = Relationship(
            actorId = ActorId(1),
            targetActorId = ActorId(2),
            following = false,
            blocking = false,
            muting = false,
            followRequesting = true,
            mutingFollowRequest = true
        )

        relationship.acceptFollowRequest()
        assertTrue(relationship.following)
        assertContainsEvent(relationship, RelationshipEvent.ACCEPT_FOLLOW.eventName)
    }

    @Test
    fun rejectFollowRequest_followRequestingがfalseになりREJECT_FOLLOWが発生する() {
        val relationship = Relationship(
            actorId = ActorId(1),
            targetActorId = ActorId(2),
            following = false,
            blocking = false,
            muting = false,
            followRequesting = true,
            mutingFollowRequest = false
        )

        relationship.rejectFollowRequest()
        assertFalse(relationship.followRequesting)
        assertContainsEvent(relationship, RelationshipEvent.REJECT_FOLLOW.eventName)
    }
}