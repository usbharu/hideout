package dev.usbharu.hideout.core.domain.model.actor

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class ActorNameTest {
    @Test
    fun blankはダメ() {
        assertThrows<IllegalArgumentException> {
            ActorName("")
        }
    }

    @Test
    fun 長過ぎるとダメ() {
        assertThrows<IllegalArgumentException> {
            ActorName("a".repeat(1000))
        }
    }

    @Test
    fun 指定外の文字は使えない() {
        assertThrows<IllegalArgumentException> {
            ActorName("あ")
        }
    }

    @Test
    fun 普通に作成できる() {
        assertDoesNotThrow {
            ActorName("test-user")
        }
    }
}