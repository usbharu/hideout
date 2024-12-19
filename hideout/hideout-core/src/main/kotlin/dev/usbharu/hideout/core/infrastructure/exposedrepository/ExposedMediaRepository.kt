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

package dev.usbharu.hideout.core.infrastructure.exposedrepository

import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.media.*
import dev.usbharu.hideout.core.infrastructure.exposed.uri
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import dev.usbharu.hideout.core.domain.model.media.Media as EntityMedia

@Repository
class ExposedMediaRepository : MediaRepository, AbstractRepository(logger) {

    override suspend fun save(media: EntityMedia): EntityMedia = query {
        Media.upsert {
            it[id] = media.id.id
            it[name] = media.name.name
            it[url] = media.url
            it[remoteUrl] = media.remoteUrl
            it[thumbnailUrl] = media.thumbnailUrl
            it[type] = media.type.name
            it[blurhash] = media.blurHash?.hash
            it[mimeType] = media.mimeType.type + "/" + media.mimeType.subtype
            it[description] = media.description?.description
            it[actorId] = media.actorId.id
        }
        return@query media
    }

    override suspend fun findById(id: MediaId): dev.usbharu.hideout.core.domain.model.media.Media? {
        return query {
            return@query Media
                .selectAll().where { Media.id eq id.id }
                .limit(1)
                .singleOrNull()
                ?.toMedia()
        }
    }

    override suspend fun findByIdIn(ids: List<MediaId>): List<dev.usbharu.hideout.core.domain.model.media.Media> {
        return query {
            return@query Media
                .selectAll()
                .where { Media.id inList ids.map { it.id } }
                .map { it.toMedia() }
        }
    }

    override suspend fun delete(media: dev.usbharu.hideout.core.domain.model.media.Media): Unit = query {
        Media.deleteWhere {
            id eq id
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ExposedMediaRepository::class.java)
    }
}

fun ResultRow.toMedia(): EntityMedia {
    val fileType = FileType.valueOf(this[Media.type])
    val mimeType = this[Media.mimeType]
    return EntityMedia(
        id = MediaId(this[Media.id]),
        name = MediaName(this[Media.name]),
        url = this[Media.url],
        remoteUrl = this[Media.remoteUrl],
        thumbnailUrl = this[Media.thumbnailUrl],
        type = fileType,
        blurHash = this[Media.blurhash]?.let { MediaBlurHash(it) },
        mimeType = MimeType(mimeType.substringBefore("/"), mimeType.substringAfter("/"), fileType),
        description = this[Media.description]?.let { MediaDescription(it) },
        actorId = ActorId(this[Media.actorId])
    )
}

fun ResultRow.toMediaOrNull(): EntityMedia? {
    val fileType = FileType.valueOf(this.getOrNull(Media.type) ?: return null)
    val mimeType = this.getOrNull(Media.mimeType) ?: return null
    return EntityMedia(
        id = MediaId(this.getOrNull(Media.id) ?: return null),
        name = MediaName(this.getOrNull(Media.name) ?: return null),
        url = this.getOrNull(Media.url) ?: return null,
        remoteUrl = this[Media.remoteUrl],
        thumbnailUrl = this[Media.thumbnailUrl],
        type = FileType.valueOf(this[Media.type]),
        blurHash = this[Media.blurhash]?.let { MediaBlurHash(it) },
        mimeType = MimeType(mimeType.substringBefore("/"), mimeType.substringAfter("/"), fileType),
        description = this[Media.description]?.let { MediaDescription(it) },
        actorId = ActorId(this[Media.actorId])
    )
}

object Media : Table("media") {
    val id = long("id")
    val name = varchar("name", 255)
    val url = uri("url", 255).uniqueIndex()
    val remoteUrl = uri("remote_url", 255).uniqueIndex().nullable()
    val thumbnailUrl = uri("thumbnail_url", 255).uniqueIndex().nullable()
    val type = varchar("type", 100)
    val blurhash = varchar("blurhash", 255).nullable()
    val mimeType = varchar("mime_type", 255)
    val description = varchar("description", 4000).nullable()
    val actorId = long("actor_id").references(Actors.id)
    override val primaryKey = PrimaryKey(id)
}
