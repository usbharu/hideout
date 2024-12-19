package dev.usbharu.hideout.core.domain.model.actor

import dev.usbharu.hideout.util.Base64Util
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.security.KeyPairGenerator

class ActorPublicKeyTest {
    @Test
    fun publicKeyから生成できる() {
        val genKeyPair = KeyPairGenerator.getInstance("RSA").genKeyPair()
        val actorPublicKey = ActorPublicKey.create(genKeyPair.public)
        val key = "-----BEGIN PUBLIC KEY-----\n" +
                Base64Util.encode(genKeyPair.public.encoded).chunked(64)
                    .joinToString("\n") + "\n-----END PUBLIC KEY-----"
        assertEquals(key, actorPublicKey.publicKey)
    }
}