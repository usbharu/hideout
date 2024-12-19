package dev.usbharu.hideout.core.domain.model.actor

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ActorDescriptionTest {
    @Test
    fun actorDescriptionがlength以上なら無視される() {
        val actorScreenName = ActorDescription("a".repeat(100000))

        assertEquals(ActorDescription.LENGTH, actorScreenName.description.length)
    }
}