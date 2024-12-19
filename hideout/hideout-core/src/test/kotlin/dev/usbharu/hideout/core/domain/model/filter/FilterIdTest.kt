package dev.usbharu.hideout.core.domain.model.filter

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test

class FilterIdTest {
    @Test
    fun filterIdは0以上である必要がある() {
        org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
            FilterId(-1)
        }
    }

    @Test
    fun filterIdが0以上なら設定できる() {
        assertDoesNotThrow {
            FilterId(1)
        }
    }
}