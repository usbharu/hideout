package dev.usbharu.hideout.core.domain.model.application

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test

class ApplicationNameTest {
    @Test
    fun applicationNameがlength以上の時エラー() {
        org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
            ApplicationName("a".repeat(1000))
        }
    }

    @Test
    fun applicationNameがlength未満の時設定できる() {
        assertDoesNotThrow {
            ApplicationName("a".repeat(100))
        }
    }
}
