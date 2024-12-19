package dev.usbharu.hideout.core.infrastructure.emojikt

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class EmojiKtUnicodeEmojiServiceTest {

    @ParameterizedTest
    @ValueSource(strings = ["❤", "👱", "☠️", "⁉️"])
    fun 絵文字の判定ができる(s: String) {
        assertTrue(EmojiKtUnicodeEmojiService().isUnicodeEmoji(s))
    }


}