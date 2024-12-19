package dev.usbharu.hideout.core.domain.model.actor

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test

class ActorPostsCountTest {
    @Test
    fun postsCountが負になることはない() {
        org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
            ActorPostsCount(-1)
        }
    }

    @Test
    fun postsCountが正の数値なら設定できる() {
        assertDoesNotThrow {
            ActorPostsCount(1)
        }
    }
}