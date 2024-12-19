package dev.usbharu.hideout.core.domain.model.actor

import dev.usbharu.hideout.util.Base64Util
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.security.KeyPairGenerator

class ActorPrivateKeyTest {
    @Test
    fun privateKeyから生成できる() {
        val genKeyPair = KeyPairGenerator.getInstance("RSA").genKeyPair()
        val actorPrivateKey = ActorPrivateKey.create(genKeyPair.private)
        val key = "-----BEGIN PRIVATE KEY-----\n" +
                Base64Util.encode(genKeyPair.private.encoded).chunked(64)
                    .joinToString("\n") + "\n-----END PRIVATE KEY-----"
        assertEquals(key, actorPrivateKey.privateKey)
    }
}