package dev.usbharu.hideout.core.domain.model.emoji

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test

class CustomEmojiIdTest {
    @Test
    fun emojiIdは0以上である必要がある() {
        org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
            CustomEmojiId(-1)
        }
    }

    @Test
    fun emojiIdは0以上なら設定できる() {
        assertDoesNotThrow {
            CustomEmojiId(1)
        }
    }
}