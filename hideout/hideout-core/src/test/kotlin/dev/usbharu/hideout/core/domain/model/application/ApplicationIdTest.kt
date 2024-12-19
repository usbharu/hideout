package dev.usbharu.hideout.core.domain.model.application

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test

class ApplicationIdTest {
    @Test
    fun applicationIdは0以上である必要がある() {
        org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
            ApplicationId(-1)
        }
    }

    @Test
    fun applicationIdが0以上なら設定できる() {
        assertDoesNotThrow {
            ApplicationId(1)
        }
    }
}