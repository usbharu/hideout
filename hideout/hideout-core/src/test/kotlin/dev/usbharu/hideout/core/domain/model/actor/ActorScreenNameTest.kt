package dev.usbharu.hideout.core.domain.model.actor

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ActorScreenNameTest {
    @Test
    fun screenNameがlengthを超えると無視される() {
        val actorScreenName = ActorScreenName("a".repeat(1000))

        assertEquals(ActorScreenName.LENGTH, actorScreenName.screenName.length)
    }
}