package dev.usbharu.hideout.core.domain.model.actor

import org.junit.jupiter.api.Test

class ActorIdTest {
    @Test
    fun idを負の数にすることはできない() {
        org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
            ActorId(-1)
        }
    }
}