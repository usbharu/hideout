package dev.usbharu.hideout.core.infrastructure.exposedrepository

import com.ninja_squad.dbsetup_kotlin.dbSetup
import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.media.*
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.db.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import utils.*
import java.net.URI
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import dev.usbharu.hideout.core.domain.model.media.Media as EntityMedia


class ExposedMediaRepositoryTest : AbstractRepositoryTest(Media) {
    @Test
    fun save_idが同じレコードが存在しないとinsert() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
            insertInto("public.actors") {
                columns(Actors.columns)
                values(
                    1,
                    "b",
                    "test-hideout-dev.usbharu.dev",
                    "b",
                    "",
                    "https://test-hideout-dev.usbharu.dev/users/b/inbox",
                    "https://test-hideout-dev.usbharu.dev/users/b/outbox",
                    "https://test-hideout-dev.usbharu.dev/users/b",
                    "-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyuMjzmQBsSxzK6NkOpZh\nWuohaUbzCY7AafXt+3+tiL6LulYNg/YRIqKc7Q/vTJE6CHrqo7RA/OqYrSMxF/LC\nf8aX5aHwJE1A2gSgCcs1IL5GJaYRlp4NcuazpBC9NO4xIrvH//jcVnZGXGWsCbls\nHXZGZdurWOF0Bl3mYN8CdupVumrGuOPs+wbI/Gh+OHw611TcXMyAwFwU2UjvPEgk\nEACW9OvJaq1K40jVCAa3b1nXt53vlXXZEUlL78L0C9xuFbJG0K/GKMBN44GyftJO\nhA95Rf1Nhd0vKDLBiRocGcARmBo9PaSCR5651gJEk5/wfLUnNAf0xj3R8LBoOhnT\nCQIDAQAB\n-----END PUBLIC KEY-----",
                    "-----BEGIN PRIVATE KEY-----\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDK4yPOZAGxLHMr\no2Q6lmFa6iFpRvMJjsBp9e37f62Ivou6Vg2D9hEiopztD+9MkToIeuqjtED86pit\nIzEX8sJ/xpflofAkTUDaBKAJyzUgvkYlphGWng1y5rOkEL007jEiu8f/+NxWdkZc\nZawJuWwddkZl26tY4XQGXeZg3wJ26lW6asa44+z7Bsj8aH44fDrXVNxczIDAXBTZ\nSO88SCQQAJb068lqrUrjSNUIBrdvWde3ne+VddkRSUvvwvQL3G4VskbQr8YowE3j\ngbJ+0k6ED3lF/U2F3S8oMsGJGhwZwBGYGj09pIJHnrnWAkSTn/B8tSc0B/TGPdHw\nsGg6GdMJAgMBAAECggEAHkEhLEb70kdOGgJLUR9D/5zYBE0eXdz/MsMyd1AH+Shs\n9AmetKsYzWDmuhp9Cp5swyn328Hmn7B+DvInVn+5YvjNhY07SbaJcVls4g5UQFXk\nu6WC4ZfKap7IyAeaUg54858r8677xcWXuByN5dn+1iU2hJGYK3Cx7rx0PRrUURYG\n2BRaEEwkcPNm9u679OOTyvTmA3NhewUuDaTMkZnnAml87uYYnmFKjQcR+S2UqOm6\nvBZ/devG4TfPBeKEAya/ba8JJ8frGOtjmR9EIliTQoxI2izeAfoGs1OsCSpuPy6s\nV5f0X3HYM7CA+Fpkt2pnixuwg96LaVr4OpVxujhNlwKBgQD1827VuKFGrneNO+c+\n4EIvh+vLh462bJiaVsMHfRhNZF1/5i8gfNJ16ST60hJo11E4riHPzi3q6GWuxOYl\nCkVKvhJ2g3mgnhoehcgnT7UBkasaC7JYd+LsFDnWOTVSJOy2OqfLdLDGAuSTN3kO\nBF4p0ZqQ/AouFNin57WNRGVZ7wKBgQDTLUZtfTkOU3G1nIMTRKmZjqdER5glzHCm\n9o/1ZsQktL+nzSXqYeoWh9fr7fkmC0k/07+SHzzfWvOhWWWlRenUVL5mj7FRq+L9\n9kDjChLR3Jr4L6Sj1iaQ+0uqDSQNYSYO9ctMjAVjFiNhiAd+S6B451Q1VbDKTCHt\nkRW9omz6hwKBgBFTsgY6eJorJl77zmG+mMsSb0kqZqJxahrNa/X2GSUyoeelxsIq\nKQWHhERrUkKykJVGpzkllFSNRMSYOIJ5g8ItO82/m2z2Vm66DAzA78aJhZ1TH6Bd\n6c2p6x0tcJU15rs7zKBnuyBoCcRZTxzur9eQXaxDJVBzxYOmrkKig+VfAoGBAMCP\n2Fiehxh5HobsYNmBEuXjHsM0RZiyA0c8LakoPFL8PodUme5PupUw6cNJDJeUUwbQ\nny8vLOK+nMnUKsu6JK5pV/VNsfM3OZU6p5Bf7ylOcEE/sHF1JVWu0CAQO3+3xmx9\n1RPH2mGwHjMhRzPy4jFdP3wi10KgiY+HbLuvEJChAoGAYCsh3UhtTzGUOlPBkmLL\n17bD0wN4J/fOv8BoXPZ8H2CdqVgWy0s+s+QaPqRxNcA6YyGymBqrmQAn1Uii25r9\nKAwVAjg3S2KDEMSI2RbMMmQJSZ1u0GkxqOUC/MMeZqBYTYxVeqcQPoqJZ0Nk7IOA\nZPFif8bVfcZqeimxrFaV6YI=\n-----END PRIVATE KEY-----",
                    "2024-09-09 17:12:03.941339",
                    "https://test-hideout-dev.usbharu.dev/users/b#main-key",
                    "https://test-hideout-dev.usbharu.dev/users/b/following",
                    "https://test-hideout-dev.usbharu.dev/users/b/followers",
                    1,
                    false,
                    0,
                    0,
                    0,
                    null,
                    "2024-09-09 17:12:03.941339",
                    false,
                    null,
                    "",
                    false,
                    null,
                    null
                )
            }
            execute(enableReferenceIntegrityConstraints)
        }.launch()
        val media = EntityMedia(
            id = MediaId(1),
            name = MediaName("name"),
            url = URI.create("https://www.example.com"),
            remoteUrl = null,
            thumbnailUrl = null,
            type = FileType.Audio,
            mimeType = MimeType("audio", "mp3", FileType.Audio),
            blurHash = null,
            description = null,
            actorId = ActorId(1)
        )
        ExposedMediaRepository().save(media)

        assertThat(assertTable)
            .row(0)
            .isEqualTo(Media.id, 1)
            .isEqualTo(Media.name, "name")
            .value(Media.url).isEqualTo("https://www.example.com")
            .isEqualTo(Media.remoteUrl, null)
            .isEqualTo(Media.thumbnailUrl, null)
            .isEqualTo(Media.type, "Audio")
            .isEqualTo(Media.mimeType, "audio/mp3")
            .isEqualTo(Media.blurhash, null)
            .isEqualTo(Media.description, null)
            .isEqualTo(Media.actorId, 1)
    }

    @Test
    fun save_idが同じレコードが存在したらupdate() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
            insertInto("public.actors") {
                columns(Actors.columns)
                values(
                    1,
                    "b",
                    "test-hideout-dev.usbharu.dev",
                    "b",
                    "",
                    "https://test-hideout-dev.usbharu.dev/users/b/inbox",
                    "https://test-hideout-dev.usbharu.dev/users/b/outbox",
                    "https://test-hideout-dev.usbharu.dev/users/b",
                    "-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyuMjzmQBsSxzK6NkOpZh\nWuohaUbzCY7AafXt+3+tiL6LulYNg/YRIqKc7Q/vTJE6CHrqo7RA/OqYrSMxF/LC\nf8aX5aHwJE1A2gSgCcs1IL5GJaYRlp4NcuazpBC9NO4xIrvH//jcVnZGXGWsCbls\nHXZGZdurWOF0Bl3mYN8CdupVumrGuOPs+wbI/Gh+OHw611TcXMyAwFwU2UjvPEgk\nEACW9OvJaq1K40jVCAa3b1nXt53vlXXZEUlL78L0C9xuFbJG0K/GKMBN44GyftJO\nhA95Rf1Nhd0vKDLBiRocGcARmBo9PaSCR5651gJEk5/wfLUnNAf0xj3R8LBoOhnT\nCQIDAQAB\n-----END PUBLIC KEY-----",
                    "-----BEGIN PRIVATE KEY-----\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDK4yPOZAGxLHMr\no2Q6lmFa6iFpRvMJjsBp9e37f62Ivou6Vg2D9hEiopztD+9MkToIeuqjtED86pit\nIzEX8sJ/xpflofAkTUDaBKAJyzUgvkYlphGWng1y5rOkEL007jEiu8f/+NxWdkZc\nZawJuWwddkZl26tY4XQGXeZg3wJ26lW6asa44+z7Bsj8aH44fDrXVNxczIDAXBTZ\nSO88SCQQAJb068lqrUrjSNUIBrdvWde3ne+VddkRSUvvwvQL3G4VskbQr8YowE3j\ngbJ+0k6ED3lF/U2F3S8oMsGJGhwZwBGYGj09pIJHnrnWAkSTn/B8tSc0B/TGPdHw\nsGg6GdMJAgMBAAECggEAHkEhLEb70kdOGgJLUR9D/5zYBE0eXdz/MsMyd1AH+Shs\n9AmetKsYzWDmuhp9Cp5swyn328Hmn7B+DvInVn+5YvjNhY07SbaJcVls4g5UQFXk\nu6WC4ZfKap7IyAeaUg54858r8677xcWXuByN5dn+1iU2hJGYK3Cx7rx0PRrUURYG\n2BRaEEwkcPNm9u679OOTyvTmA3NhewUuDaTMkZnnAml87uYYnmFKjQcR+S2UqOm6\nvBZ/devG4TfPBeKEAya/ba8JJ8frGOtjmR9EIliTQoxI2izeAfoGs1OsCSpuPy6s\nV5f0X3HYM7CA+Fpkt2pnixuwg96LaVr4OpVxujhNlwKBgQD1827VuKFGrneNO+c+\n4EIvh+vLh462bJiaVsMHfRhNZF1/5i8gfNJ16ST60hJo11E4riHPzi3q6GWuxOYl\nCkVKvhJ2g3mgnhoehcgnT7UBkasaC7JYd+LsFDnWOTVSJOy2OqfLdLDGAuSTN3kO\nBF4p0ZqQ/AouFNin57WNRGVZ7wKBgQDTLUZtfTkOU3G1nIMTRKmZjqdER5glzHCm\n9o/1ZsQktL+nzSXqYeoWh9fr7fkmC0k/07+SHzzfWvOhWWWlRenUVL5mj7FRq+L9\n9kDjChLR3Jr4L6Sj1iaQ+0uqDSQNYSYO9ctMjAVjFiNhiAd+S6B451Q1VbDKTCHt\nkRW9omz6hwKBgBFTsgY6eJorJl77zmG+mMsSb0kqZqJxahrNa/X2GSUyoeelxsIq\nKQWHhERrUkKykJVGpzkllFSNRMSYOIJ5g8ItO82/m2z2Vm66DAzA78aJhZ1TH6Bd\n6c2p6x0tcJU15rs7zKBnuyBoCcRZTxzur9eQXaxDJVBzxYOmrkKig+VfAoGBAMCP\n2Fiehxh5HobsYNmBEuXjHsM0RZiyA0c8LakoPFL8PodUme5PupUw6cNJDJeUUwbQ\nny8vLOK+nMnUKsu6JK5pV/VNsfM3OZU6p5Bf7ylOcEE/sHF1JVWu0CAQO3+3xmx9\n1RPH2mGwHjMhRzPy4jFdP3wi10KgiY+HbLuvEJChAoGAYCsh3UhtTzGUOlPBkmLL\n17bD0wN4J/fOv8BoXPZ8H2CdqVgWy0s+s+QaPqRxNcA6YyGymBqrmQAn1Uii25r9\nKAwVAjg3S2KDEMSI2RbMMmQJSZ1u0GkxqOUC/MMeZqBYTYxVeqcQPoqJZ0Nk7IOA\nZPFif8bVfcZqeimxrFaV6YI=\n-----END PRIVATE KEY-----",
                    "2024-09-09 17:12:03.941339",
                    "https://test-hideout-dev.usbharu.dev/users/b#main-key",
                    "https://test-hideout-dev.usbharu.dev/users/b/following",
                    "https://test-hideout-dev.usbharu.dev/users/b/followers",
                    1,
                    false,
                    0,
                    0,
                    0,
                    null,
                    "2024-09-09 17:12:03.941339",
                    false,
                    null,
                    "",
                    false,
                    null,
                    null
                )
            }
            insertInto("public.media") {
                columns(
                    "id",
                    "name",
                    "url",
                    "remote_url",
                    "thumbnail_url",
                    "type",
                    "blurhash",
                    "mime_type",
                    "description",
                    "actor_id"
                )
                values(
                    1,
                    "pnc__picked_media_256f8e6d-68cd-4a76-bb38-57e35f6ca8c6.jpg",
                    "http://localhost:8081/files/1833054358862827520.jpeg",
                    null,
                    "http://localhost:8081/files/thumbnail-1833054358862827520.jpeg",
                    "Image",
                    "U\$JuAZWBxut7~qoLoft6j]t7Rjj[RjayWBay",
                    "image/jpeg",
                    null,
                    1
                )
            }
            execute(enableReferenceIntegrityConstraints)
        }.launch()

        val media = EntityMedia(
            id = MediaId(1),
            name = MediaName("name"),
            url = URI.create("https://www.example.com"),
            remoteUrl = null,
            thumbnailUrl = null,
            type = FileType.Audio,
            mimeType = MimeType("audio", "mp3", FileType.Audio),
            blurHash = null,
            description = null,
            actorId = ActorId(1)
        )
        ExposedMediaRepository().save(media)

        assertThat(assertTable)
            .row(0)
            .isEqualTo(Media.id, 1)
            .isEqualTo(Media.name, "name")
            .value(Media.url).isEqualTo("https://www.example.com")
            .isEqualTo(Media.remoteUrl, null)
            .isEqualTo(Media.thumbnailUrl, null)
            .isEqualTo(Media.type, "Audio")
            .isEqualTo(Media.mimeType, "audio/mp3")
            .isEqualTo(Media.blurhash, null)
            .isEqualTo(Media.description, null)
            .isEqualTo(Media.actorId, 1)
    }

    @Test
    fun delete_削除される() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
            insertInto("public.media") {
                columns(
                    "id",
                    "name",
                    "url",
                    "remote_url",
                    "thumbnail_url",
                    "type",
                    "blurhash",
                    "mime_type",
                    "description",
                    "actor_id"
                )
                values(
                    1,
                    "pnc__picked_media_256f8e6d-68cd-4a76-bb38-57e35f6ca8c6.jpg",
                    "http://localhost:8081/files/1833054358862827520.jpeg",
                    null,
                    "http://localhost:8081/files/thumbnail-1833054358862827520.jpeg",
                    "Image",
                    "U\$JuAZWBxut7~qoLoft6j]t7Rjj[RjayWBay",
                    "image/jpeg",
                    null,
                    1
                )
            }
            execute(enableReferenceIntegrityConstraints)
        }.launch()
        val media = EntityMedia(
            id = MediaId(1),
            name = MediaName("name"),
            url = URI.create("https://www.example.com"),
            remoteUrl = null,
            thumbnailUrl = null,
            type = FileType.Audio,
            mimeType = MimeType("audio", "mp3", FileType.Audio),
            blurHash = null,
            description = null,
            actorId = ActorId(1)
        )

        change.withSuspend {
            ExposedMediaRepository().delete(media)
        }

        assertThat(change)
            .changeOfDeletionOnTable(Media.tableName)
            .rowAtStartPoint()
            .value(Media.id.name)
            .isEqualTo(1)
    }

    @Test
    fun findById_指定されたIdで存在したら返す() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
            insertInto("public.media") {
                columns(
                    "id",
                    "name",
                    "url",
                    "remote_url",
                    "thumbnail_url",
                    "type",
                    "blurhash",
                    "mime_type",
                    "description",
                    "actor_id"
                )
                values(
                    1,
                    "pnc__picked_media_256f8e6d-68cd-4a76-bb38-57e35f6ca8c6.jpg",
                    "http://localhost:8081/files/1833054358862827520.jpeg",
                    "http://localhost:8081/files/183305453584862827520.jpeg",
                    "http://localhost:8081/files/thumbnail-1833054358862827520.jpeg",
                    "Image",
                    "U\$JuAZWBxut7~qoLoft6j]t7Rjj[RjayWBay",
                    "image/jpeg",
                    null,
                    1
                )
            }
            execute(enableReferenceIntegrityConstraints)
        }.launch()

        val expect = EntityMedia(
            id = MediaId(1),
            name = MediaName("pnc__picked_media_256f8e6d-68cd-4a76-bb38-57e35f6ca8c6.jpg"),
            url = URI.create("http://localhost:8081/files/1833054358862827520.jpeg"),
            remoteUrl = URI.create("http://localhost:8081/files/183305453584862827520.jpeg"),
            thumbnailUrl = URI.create("http://localhost:8081/files/thumbnail-1833054358862827520.jpeg"),
            type = FileType.Image,
            mimeType = MimeType("image", "jpeg", FileType.Image),
            blurHash = MediaBlurHash("U\$JuAZWBxut7~qoLoft6j]t7Rjj[RjayWBay"),
            description = null,
            actorId = ActorId(1)
        )

        val actual = ExposedMediaRepository().findById(MediaId(1))

        assertNotNull(actual)
        assertEquals(expect, actual)
        assertEquals(expect.id, actual.id)
        assertEquals(expect.name, actual.name)
        assertEquals(expect.url, actual.url)
        assertEquals(expect.remoteUrl, actual.remoteUrl)
        assertEquals(expect.thumbnailUrl, actual.thumbnailUrl)
        assertEquals(expect.type, actual.type)
        assertEquals(expect.mimeType, actual.mimeType)
        assertEquals(expect.blurHash, actual.blurHash)
        assertEquals(expect.description, actual.description)
        assertEquals(expect.actorId, actual.actorId)
    }

    @Test
    fun findById_指定されたIdで存在しないとnull() = runTest {
        assertNull(ExposedMediaRepository().findById(MediaId(1)))
    }

    @Test
    fun findByIdIn_指定されたIdすべて返す() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
            insertInto("public.media") {
                columns(
                    "id",
                    "name",
                    "url",
                    "remote_url",
                    "thumbnail_url",
                    "type",
                    "blurhash",
                    "mime_type",
                    "description",
                    "actor_id"
                )
                values(
                    1,
                    "pnc__picked_media_256f8e6d-68cd-4a76-bb38-57e35f6ca8c6.jpg",
                    "http://localhost:8081/files/1833054358862827520.jpeg",
                    null,
                    null,
                    "Image",
                    "U\$JuAZWBxut7~qoLoft6j]t7Rjj[RjayWBay",
                    "image/jpeg",
                    "",
                    1
                )
                values(
                    3,
                    "pnc__picked_media_256f8e6d-68cd-4a76-bb38-57e35f6ca8c6.jpg",
                    "http://localhost:8081/files/18330354358862827520.jpeg",
                    null,
                    "http://localhost:8081/files/thumbn3ail-1833054358862827520.jpeg",
                    "Image",
                    null,
                    "image/jpeg",
                    null,
                    1
                )
                values(
                    2,
                    "pnc__picked_media_256f8e6d-68cd-4a76-bb38-57e35f6ca8c6.jpg",
                    "http://localhost:8081/files/18330545358862827520.jpeg",
                    "http://localhost:8081/files/183305453584862827520.jpeg",
                    null,
                    "Image",
                    "U\$JuAZWBxut7~qoLoft6j]t7Rjj[RjayWBay",
                    "image/jpeg",
                    null,
                    1
                )
            }
            execute(enableReferenceIntegrityConstraints)
        }.launch()

        val actual = ExposedMediaRepository().findByIdIn(listOf(MediaId(1), MediaId(3)))

        assertThat(actual)
            .hasSize(2)
    }
}