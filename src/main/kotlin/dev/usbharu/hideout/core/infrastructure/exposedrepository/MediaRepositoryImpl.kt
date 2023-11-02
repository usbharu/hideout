package dev.usbharu.hideout.core.infrastructure.exposedrepository

import dev.usbharu.hideout.application.service.id.IdGenerateService
import dev.usbharu.hideout.core.domain.exception.FailedToGetResourcesException
import dev.usbharu.hideout.core.domain.model.media.MediaRepository
import dev.usbharu.hideout.core.service.media.FileType
import dev.usbharu.hideout.util.singleOr
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.springframework.stereotype.Repository
import dev.usbharu.hideout.core.domain.model.media.Media as EntityMedia

@Repository
class MediaRepositoryImpl(private val idGenerateService: IdGenerateService) : MediaRepository {
    override suspend fun generateId(): Long = idGenerateService.generateId()

    override suspend fun save(media: EntityMedia): EntityMedia {
        if (Media.select {
                Media.id eq media.id
            }.singleOrNull() != null
        ) {
            Media.update({ Media.id eq media.id }) {
                it[name] = media.name
                it[url] = media.url
                it[remoteUrl] = media.remoteUrl
                it[thumbnailUrl] = media.thumbnailUrl
                it[type] = media.type.ordinal
                it[blurhash] = media.blurHash
            }
        } else {
            Media.insert {
                it[id] = media.id
                it[name] = media.name
                it[url] = media.url
                it[remoteUrl] = media.remoteUrl
                it[thumbnailUrl] = media.thumbnailUrl
                it[type] = media.type.ordinal
                it[blurhash] = media.blurHash
            }
        }
        return media
    }

    override suspend fun findById(id: Long): EntityMedia {
        return Media
            .select {
                Media.id eq id
            }
            .singleOr {
                FailedToGetResourcesException("id: $id was not found.")
            }.toMedia()
    }

    override suspend fun delete(id: Long) {
        Media.deleteWhere {
            Media.id eq id
        }
    }
}

fun ResultRow.toMedia(): EntityMedia {
    return EntityMedia(
        id = this[Media.id],
        name = this[Media.name],
        url = this[Media.url],
        remoteUrl = this[Media.remoteUrl],
        thumbnailUrl = this[Media.thumbnailUrl],
        type = FileType.values().first { it.ordinal == this[Media.type] },
        blurHash = this[Media.blurhash],
    )
}

fun ResultRow.toMediaOrNull(): EntityMedia? {
    return EntityMedia(
        id = this.getOrNull(Media.id) ?: return null,
        name = this.getOrNull(Media.name) ?: return null,
        url = this.getOrNull(Media.url) ?: return null,
        remoteUrl = this[Media.remoteUrl],
        thumbnailUrl = this[Media.thumbnailUrl],
        type = FileType.values().first { it.ordinal == this.getOrNull(Media.type) },
        blurHash = this[Media.blurhash],
    )
}

object Media : Table("media") {
    val id = long("id")
    val name = varchar("name", 255)
    val url = varchar("url", 255)
    val remoteUrl = varchar("remote_url", 255).nullable()
    val thumbnailUrl = varchar("thumbnail_url", 255).nullable()
    val type = integer("type")
    val blurhash = varchar("blurhash", 255).nullable()
    override val primaryKey = PrimaryKey(id)
}