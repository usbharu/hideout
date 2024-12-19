package dev.usbharu.hideout.core.domain.model.support.domain

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows


class DomainTest {
    @Test
    fun `1000超過の長さは失敗`() {
        assertThrows<IllegalArgumentException> {
            Domain("a".repeat(1001))
        }
    }
}