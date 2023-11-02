package dev.usbharu.hideout.core.infrastructure.exposedquery

import dev.usbharu.hideout.core.domain.model.user.User
import dev.usbharu.hideout.core.infrastructure.exposedrepository.Users
import dev.usbharu.hideout.core.infrastructure.exposedrepository.UsersFollowers
import dev.usbharu.hideout.core.query.FollowerQueryService
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class FollowerQueryServiceImpl(private val userBuilder: User.UserBuilder) : FollowerQueryService {
    override suspend fun findFollowersById(id: Long): List<User> {
        val followers = Users.alias("FOLLOWERS")
        return Users.innerJoin(
            otherTable = UsersFollowers,
            onColumn = { Users.id },
            otherColumn = { userId }
        )
            .innerJoin(
                otherTable = followers,
                onColumn = { UsersFollowers.followerId },
                otherColumn = { followers[Users.id] }
            )
            .slice(
                followers[Users.id],
                followers[Users.name],
                followers[Users.domain],
                followers[Users.screenName],
                followers[Users.description],
                followers[Users.password],
                followers[Users.inbox],
                followers[Users.outbox],
                followers[Users.url],
                followers[Users.publicKey],
                followers[Users.privateKey],
                followers[Users.createdAt],
                followers[Users.keyId],
                followers[Users.following],
                followers[Users.followers]
            )
            .select { Users.id eq id }
            .map {
                userBuilder.of(
                    id = it[followers[Users.id]],
                    name = it[followers[Users.name]],
                    domain = it[followers[Users.domain]],
                    screenName = it[followers[Users.screenName]],
                    description = it[followers[Users.description]],
                    password = it[followers[Users.password]],
                    inbox = it[followers[Users.inbox]],
                    outbox = it[followers[Users.outbox]],
                    url = it[followers[Users.url]],
                    publicKey = it[followers[Users.publicKey]],
                    privateKey = it[followers[Users.privateKey]],
                    createdAt = Instant.ofEpochMilli(it[followers[Users.createdAt]]),
                    keyId = it[followers[Users.keyId]],
                    followers = it[followers[Users.followers]],
                    following = it[followers[Users.following]]
                )
            }
    }

    override suspend fun findFollowersByNameAndDomain(name: String, domain: String): List<User> {
        val followers = Users.alias("FOLLOWERS")
        return Users.innerJoin(
            otherTable = UsersFollowers,
            onColumn = { id },
            otherColumn = { userId }
        )
            .innerJoin(
                otherTable = followers,
                onColumn = { UsersFollowers.followerId },
                otherColumn = { followers[Users.id] }
            )
            .slice(
                followers[Users.id],
                followers[Users.name],
                followers[Users.domain],
                followers[Users.screenName],
                followers[Users.description],
                followers[Users.password],
                followers[Users.inbox],
                followers[Users.outbox],
                followers[Users.url],
                followers[Users.publicKey],
                followers[Users.privateKey],
                followers[Users.createdAt],
                followers[Users.keyId],
                followers[Users.following],
                followers[Users.followers]
            )
            .select { Users.name eq name and (Users.domain eq domain) }
            .map {
                userBuilder.of(
                    id = it[followers[Users.id]],
                    name = it[followers[Users.name]],
                    domain = it[followers[Users.domain]],
                    screenName = it[followers[Users.screenName]],
                    description = it[followers[Users.description]],
                    password = it[followers[Users.password]],
                    inbox = it[followers[Users.inbox]],
                    outbox = it[followers[Users.outbox]],
                    url = it[followers[Users.url]],
                    publicKey = it[followers[Users.publicKey]],
                    privateKey = it[followers[Users.privateKey]],
                    createdAt = Instant.ofEpochMilli(it[followers[Users.createdAt]]),
                    keyId = it[followers[Users.keyId]],
                    followers = it[followers[Users.followers]],
                    following = it[followers[Users.following]]
                )
            }
    }

    override suspend fun findFollowingById(id: Long): List<User> {
        val followers = Users.alias("FOLLOWERS")
        return Users.innerJoin(
            otherTable = UsersFollowers,
            onColumn = { Users.id },
            otherColumn = { userId }
        )
            .innerJoin(
                otherTable = followers,
                onColumn = { UsersFollowers.followerId },
                otherColumn = { followers[Users.id] }
            )
            .slice(
                followers[Users.id],
                followers[Users.name],
                followers[Users.domain],
                followers[Users.screenName],
                followers[Users.description],
                followers[Users.password],
                followers[Users.inbox],
                followers[Users.outbox],
                followers[Users.url],
                followers[Users.publicKey],
                followers[Users.privateKey],
                followers[Users.createdAt],
                followers[Users.keyId],
                followers[Users.following],
                followers[Users.followers]
            )
            .select { followers[Users.id] eq id }
            .map {
                userBuilder.of(
                    id = it[followers[Users.id]],
                    name = it[followers[Users.name]],
                    domain = it[followers[Users.domain]],
                    screenName = it[followers[Users.screenName]],
                    description = it[followers[Users.description]],
                    password = it[followers[Users.password]],
                    inbox = it[followers[Users.inbox]],
                    outbox = it[followers[Users.outbox]],
                    url = it[followers[Users.url]],
                    publicKey = it[followers[Users.publicKey]],
                    privateKey = it[followers[Users.privateKey]],
                    createdAt = Instant.ofEpochMilli(it[followers[Users.createdAt]]),
                    keyId = it[followers[Users.keyId]],
                    followers = it[followers[Users.followers]],
                    following = it[followers[Users.following]]
                )
            }
    }

    override suspend fun findFollowingByNameAndDomain(name: String, domain: String): List<User> {
        val followers = Users.alias("FOLLOWERS")
        return Users.innerJoin(
            otherTable = UsersFollowers,
            onColumn = { id },
            otherColumn = { userId }
        )
            .innerJoin(
                otherTable = followers,
                onColumn = { UsersFollowers.followerId },
                otherColumn = { followers[Users.id] }
            )
            .slice(
                followers[Users.id],
                followers[Users.name],
                followers[Users.domain],
                followers[Users.screenName],
                followers[Users.description],
                followers[Users.password],
                followers[Users.inbox],
                followers[Users.outbox],
                followers[Users.url],
                followers[Users.publicKey],
                followers[Users.privateKey],
                followers[Users.createdAt],
                followers[Users.keyId],
                followers[Users.following],
                followers[Users.followers]
            )
            .select { followers[Users.name] eq name and (followers[Users.domain] eq domain) }
            .map {
                userBuilder.of(
                    id = it[followers[Users.id]],
                    name = it[followers[Users.name]],
                    domain = it[followers[Users.domain]],
                    screenName = it[followers[Users.screenName]],
                    description = it[followers[Users.description]],
                    password = it[followers[Users.password]],
                    inbox = it[followers[Users.inbox]],
                    outbox = it[followers[Users.outbox]],
                    url = it[followers[Users.url]],
                    publicKey = it[followers[Users.publicKey]],
                    privateKey = it[followers[Users.privateKey]],
                    createdAt = Instant.ofEpochMilli(it[followers[Users.createdAt]]),
                    keyId = it[followers[Users.keyId]],
                    followers = it[followers[Users.followers]],
                    following = it[followers[Users.following]]
                )
            }
    }

    override suspend fun appendFollower(user: Long, follower: Long) {
        UsersFollowers.insert {
            it[userId] = user
            it[followerId] = follower
        }
    }

    override suspend fun removeFollower(user: Long, follower: Long) {
        UsersFollowers.deleteWhere { userId eq user and (followerId eq follower) }
    }

    override suspend fun alreadyFollow(userId: Long, followerId: Long): Boolean {
        return UsersFollowers.select { UsersFollowers.userId eq userId or (UsersFollowers.followerId eq followerId) }
            .empty()
            .not()
    }
}