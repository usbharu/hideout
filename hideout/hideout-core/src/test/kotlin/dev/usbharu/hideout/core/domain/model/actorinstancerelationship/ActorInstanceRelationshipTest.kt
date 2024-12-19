package dev.usbharu.hideout.core.domain.model.actorinstancerelationship

import dev.usbharu.hideout.core.domain.event.actorinstancerelationship.ActorInstanceRelationshipEvent
import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.instance.InstanceId
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import utils.AssertDomainEvent.assertContainsEvent

class ActorInstanceRelationshipTest {
    @Test
    fun blockするとBLOCKイベントが発生する() {
        val actorInstanceRelationship = ActorInstanceRelationship(ActorId(1), InstanceId(2), false)

        actorInstanceRelationship.block()

        assertContainsEvent(actorInstanceRelationship, ActorInstanceRelationshipEvent.BLOCK.eventName)
        assertTrue(actorInstanceRelationship.blocking)
    }

    @Test
    fun muteするとMUTEイベントが発生する() {
        val actorInstanceRelationship = ActorInstanceRelationship(ActorId(1), InstanceId(2), false)

        actorInstanceRelationship.mute()

        assertContainsEvent(actorInstanceRelationship, ActorInstanceRelationshipEvent.MUTE.eventName)
        assertTrue(actorInstanceRelationship.muting)
    }

    @Test
    fun unmuteするとUNMUTEイベントが発生する() {
        val actorInstanceRelationship = ActorInstanceRelationship(ActorId(1), InstanceId(2), muting = true)

        actorInstanceRelationship.unmute()

        assertContainsEvent(actorInstanceRelationship, ActorInstanceRelationshipEvent.UNMUTE.eventName)
        assertFalse(actorInstanceRelationship.muting)
    }

    @Test
    fun unblockで解除される() {
        val actorInstanceRelationship = ActorInstanceRelationship(ActorId(1), InstanceId(2), true)

        actorInstanceRelationship.unblock()

        assertFalse(actorInstanceRelationship.blocking)
    }

    @Test
    fun doNotSendPrivateで設定される() {
        val actorInstanceRelationship = ActorInstanceRelationship(ActorId(1), InstanceId(2))

        actorInstanceRelationship.doNotSendPrivate()

        assertTrue(actorInstanceRelationship.doNotSendPrivate)
    }

    @Test
    fun doSendPrivateで解除される() {
        val actorInstanceRelationship = ActorInstanceRelationship(ActorId(1), InstanceId(2), doNotSendPrivate = true)

        actorInstanceRelationship.doSendPrivate()

        assertFalse(actorInstanceRelationship.doNotSendPrivate)
    }

    @Test
    fun defaultで全部falseが作られる() {
        val default = ActorInstanceRelationship.default(ActorId(1), InstanceId(2))

        assertFalse(default.muting)
        assertFalse(default.blocking)
        assertFalse(default.doNotSendPrivate)
    }
}