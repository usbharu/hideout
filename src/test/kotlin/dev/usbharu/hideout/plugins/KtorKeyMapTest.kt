package dev.usbharu.hideout.plugins

import dev.usbharu.hideout.domain.model.User
import dev.usbharu.hideout.domain.model.UserAuthentication
import dev.usbharu.hideout.domain.model.UserAuthenticationEntity
import dev.usbharu.hideout.domain.model.UserEntity
import dev.usbharu.hideout.repository.IUserAuthRepository
import dev.usbharu.hideout.repository.IUserRepository
import dev.usbharu.hideout.service.UserAuthService
import org.junit.jupiter.api.Test
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.util.*

class KtorKeyMapTest {

    @Test
    fun getPrivateKey() {
        val ktorKeyMap = KtorKeyMap(UserAuthService(object : IUserRepository {
            override suspend fun create(user: User): UserEntity {
                TODO("Not yet implemented")
            }

            override suspend fun findById(id: Long): UserEntity? {
                TODO("Not yet implemented")
            }

            override suspend fun findByName(name: String): UserEntity? {
                return UserEntity(1, "test", "localhost", "test", "")
            }

            override suspend fun update(userEntity: UserEntity) {
                TODO("Not yet implemented")
            }

            override suspend fun delete(id: Long) {
                TODO("Not yet implemented")
            }

            override suspend fun findAll(): List<User> {
                TODO("Not yet implemented")
            }

            override suspend fun findAllByLimitAndByOffset(limit: Int, offset: Long): List<UserEntity> {
                TODO("Not yet implemented")
            }

            override suspend fun createFollower(id: Long, follower: Long) {
                TODO("Not yet implemented")
            }

            override suspend fun deleteFollower(id: Long, follower: Long) {
                TODO("Not yet implemented")
            }

            override suspend fun findFollowersById(id: Long): List<UserEntity> {
                TODO("Not yet implemented")
            }

        }, object : IUserAuthRepository {
            override suspend fun create(userAuthentication: UserAuthentication): UserAuthenticationEntity {
                TODO("Not yet implemented")
            }

            override suspend fun findById(id: Long): UserAuthenticationEntity? {
                TODO("Not yet implemented")
            }

            override suspend fun update(userAuthenticationEntity: UserAuthenticationEntity) {
                TODO("Not yet implemented")
            }

            override suspend fun delete(id: Long) {
                TODO("Not yet implemented")
            }

            override suspend fun findByUserId(id: Long): UserAuthenticationEntity? {
                val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
                keyPairGenerator.initialize(1024)
                val generateKeyPair = keyPairGenerator.generateKeyPair()
                return UserAuthenticationEntity(
                    1, 1, "test", (generateKeyPair.public as RSAPublicKey).toPem(),
                    (generateKeyPair.private as RSAPrivateKey).toPem()
                )
            }
        }))

        ktorKeyMap.getPrivateKey("test")
    }

    private fun RSAPublicKey.toPem(): String {
        return "-----BEGIN RSA PUBLIC KEY-----" +
                Base64.getEncoder().encodeToString(encoded) +
                "-----END RSA PUBLIC KEY-----"
    }

    private fun RSAPrivateKey.toPem(): String {
        return "-----BEGIN RSA PRIVATE KEY-----" +
                Base64.getEncoder().encodeToString(encoded) +
                "-----END RSA PRIVATE KEY-----"
    }
}
