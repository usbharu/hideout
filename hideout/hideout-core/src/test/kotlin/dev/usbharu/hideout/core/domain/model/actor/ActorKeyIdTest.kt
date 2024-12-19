package dev.usbharu.hideout.core.domain.model.actor

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ActorKeyIdTest {
    @Test
    fun keyIdはblankではいけない() {
        assertThrows<IllegalArgumentException> {
            ActorKeyId("")
        }

        assertThrows<IllegalArgumentException> {
            ActorKeyId("   ")
        }
    }

    @Test
    fun keyIdがblankでなければ作成できる() {
        assertDoesNotThrow {
            ActorKeyId("aiueo")
        }
    }
}