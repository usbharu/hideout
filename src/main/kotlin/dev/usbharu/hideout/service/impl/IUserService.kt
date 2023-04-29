package dev.usbharu.hideout.service.impl

import dev.usbharu.hideout.domain.model.hideout.dto.RemoteUserCreateDto
import dev.usbharu.hideout.domain.model.hideout.dto.UserCreateDto
import dev.usbharu.hideout.domain.model.hideout.entity.User

interface IUserService {
    suspend fun findAll(limit: Int? = 100, offset: Long? = 0): List<User>

    suspend fun findById(id: Long): User

    suspend fun findByIds(ids: List<Long>): List<User>

    suspend fun findByName(name: String): List<User>

    suspend fun findByNameLocalUser(name: String): User

    suspend fun findByNameAndDomains(names: List<Pair<String, String>>): List<User>

    suspend fun findByUrl(url: String): User

    suspend fun findByUrls(urls: List<String>): List<User>

    suspend fun usernameAlreadyUse(username: String): Boolean

    suspend fun createLocalUser(user: UserCreateDto): User

    suspend fun createRemoteUser(user: RemoteUserCreateDto): User

    suspend fun findFollowersById(id: Long): List<User>

    suspend fun addFollowers(id: Long, follower: Long)
}
