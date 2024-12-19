package dev.usbharu.hideout.core.domain.model.application

import org.junit.jupiter.api.Test

class ApplicationTest {
    @Test
    fun インスタンスを生成できる() {
        Application(
            ApplicationId(1),
            ApplicationName("aiueo")
        )
    }
}