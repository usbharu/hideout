package dev.usbharu.hideout.core.domain.model.actor

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ActorRelationshipCountTest {
    @Test
    fun relationshipCountが負になることはない() {
        assertThrows<IllegalArgumentException> {
            ActorRelationshipCount(-1)
        }
    }

    @Test
    fun relationshipCountが正の数値なら設定できる() {
        assertDoesNotThrow {
            ActorRelationshipCount(1)
        }
    }
}