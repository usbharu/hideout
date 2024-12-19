/*
 * Copyright (C) 2024 usbharu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.usbharu.hideout.util

import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec

object RsaUtil {
    private val replaceHeaderAndFooterRegex = Regex("-----.*?-----")

    fun decodeRsaPublicKey(byteArray: ByteArray): RSAPublicKey {
        val x509EncodedKeySpec = X509EncodedKeySpec(byteArray)
        return KeyFactory.getInstance("RSA").generatePublic(x509EncodedKeySpec) as RSAPublicKey
    }

    fun decodeRsaPublicKey(encoded: String): RSAPublicKey = decodeRsaPublicKey(Base64Util.decode(encoded))

    fun decodeRsaPublicKeyPem(pem: String): RSAPublicKey {
        val replace = pem.replace(replaceHeaderAndFooterRegex, "")
            .replace("\n", "")
        return decodeRsaPublicKey(replace)
    }

    fun decodeRsaPrivateKey(byteArray: ByteArray): RSAPrivateKey {
        val pkcS8EncodedKeySpec = PKCS8EncodedKeySpec(byteArray)
        return KeyFactory.getInstance("RSA").generatePrivate(pkcS8EncodedKeySpec) as RSAPrivateKey
    }

    fun decodeRsaPrivateKey(encoded: String): RSAPrivateKey = decodeRsaPrivateKey(Base64Util.decode(encoded))

    fun encodeRsaPublicKey(publicKey: RSAPublicKey): String = Base64Util.encode(publicKey.encoded)

    fun encodeRsaPrivateKey(privateKey: RSAPrivateKey): String = Base64Util.encode(privateKey.encoded)
}
