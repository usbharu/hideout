package dev.usbharu.hideout.core.infrastructure.exposedrepository

import com.ninja_squad.dbsetup_kotlin.dbSetup
import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.emoji.CustomEmojiId
import dev.usbharu.hideout.core.domain.model.instance.InstanceId
import dev.usbharu.hideout.core.domain.model.media.MediaId
import dev.usbharu.hideout.core.domain.model.post.*
import dev.usbharu.hideout.core.domain.model.support.page.Page
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEventPublisher
import dev.usbharu.hideout.core.infrastructure.exposed.PostQueryMapper
import dev.usbharu.hideout.core.infrastructure.exposed.PostResultRowMapper
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.db.api.Assertions.assertThat
import org.assertj.db.type.Changes
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import utils.*
import java.net.URI
import java.sql.Timestamp
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@ExtendWith(MockitoExtension::class)
class ExposedPostRepositoryTest : AbstractRepositoryTest(Posts) {

    @InjectMocks
    lateinit var repository: ExposedPostRepository

    @Mock
    lateinit var domainEventPublisher: DomainEventPublisher

    @Spy
    val postQueryMapper = PostQueryMapper(PostResultRowMapper())

    @Test
    fun save_idが同じレコードがない場合はinsert() = runTest {
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
                values(
                    2,
                    "a",
                    "test-hideout-dev.usbharu.dev",
                    "a",
                    "",
                    "https://test-hideout-dev.usbharu.dev/users/a/inbox",
                    "https://test-hideout-dev.usbharu.dev/users/a/outbox",
                    "https://test-hideout-dev.usbharu.dev/users/a",
                    "-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyuMjzmQBsSxzK6NkOpZh\nWuohaUbzCY7AafXt+3+tiL6LulYNg/YRIqKc7Q/vTJE6CHrqo7RA/OqYrSMxF/LC\nf8aX5aHwJE1A2gSgCcs1IL5GJaYRlp4NcuazpBC9NO4xIrvH//jcVnZGXGWsCbls\nHXZGZdurWOF0Bl3mYN8CdupVumrGuOPs+wbI/Gh+OHw611TcXMyAwFwU2UjvPEgk\nEACW9OvJaq1K40jVCAa3b1nXt53vlXXZEUlL78L0C9xuFbJG0K/GKMBN44GyftJO\nhA95Rf1Nhd0vKDLBiRocGcARmBo9PaSCR5651gJEk5/wfLUnNAf0xj3R8LBoOhnT\nCQIDAQAB\n-----END PUBLIC KEY-----",
                    "-----BEGIN PRIVATE KEY-----\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDK4yPOZAGxLHMr\no2Q6lmFa6iFpRvMJjsBp9e37f62Ivou6Vg2D9hEiopztD+9MkToIeuqjtED86pit\nIzEX8sJ/xpflofAkTUDaBKAJyzUgvkYlphGWng1y5rOkEL007jEiu8f/+NxWdkZc\nZawJuWwddkZl26tY4XQGXeZg3wJ26lW6asa44+z7Bsj8aH44fDrXVNxczIDAXBTZ\nSO88SCQQAJb068lqrUrjSNUIBrdvWde3ne+VddkRSUvvwvQL3G4VskbQr8YowE3j\ngbJ+0k6ED3lF/U2F3S8oMsGJGhwZwBGYGj09pIJHnrnWAkSTn/B8tSc0B/TGPdHw\nsGg6GdMJAgMBAAECggEAHkEhLEb70kdOGgJLUR9D/5zYBE0eXdz/MsMyd1AH+Shs\n9AmetKsYzWDmuhp9Cp5swyn328Hmn7B+DvInVn+5YvjNhY07SbaJcVls4g5UQFXk\nu6WC4ZfKap7IyAeaUg54858r8677xcWXuByN5dn+1iU2hJGYK3Cx7rx0PRrUURYG\n2BRaEEwkcPNm9u679OOTyvTmA3NhewUuDaTMkZnnAml87uYYnmFKjQcR+S2UqOm6\nvBZ/devG4TfPBeKEAya/ba8JJ8frGOtjmR9EIliTQoxI2izeAfoGs1OsCSpuPy6s\nV5f0X3HYM7CA+Fpkt2pnixuwg96LaVr4OpVxujhNlwKBgQD1827VuKFGrneNO+c+\n4EIvh+vLh462bJiaVsMHfRhNZF1/5i8gfNJ16ST60hJo11E4riHPzi3q6GWuxOYl\nCkVKvhJ2g3mgnhoehcgnT7UBkasaC7JYd+LsFDnWOTVSJOy2OqfLdLDGAuSTN3kO\nBF4p0ZqQ/AouFNin57WNRGVZ7wKBgQDTLUZtfTkOU3G1nIMTRKmZjqdER5glzHCm\n9o/1ZsQktL+nzSXqYeoWh9fr7fkmC0k/07+SHzzfWvOhWWWlRenUVL5mj7FRq+L9\n9kDjChLR3Jr4L6Sj1iaQ+0uqDSQNYSYO9ctMjAVjFiNhiAd+S6B451Q1VbDKTCHt\nkRW9omz6hwKBgBFTsgY6eJorJl77zmG+mMsSb0kqZqJxahrNa/X2GSUyoeelxsIq\nKQWHhERrUkKykJVGpzkllFSNRMSYOIJ5g8ItO82/m2z2Vm66DAzA78aJhZ1TH6Bd\n6c2p6x0tcJU15rs7zKBnuyBoCcRZTxzur9eQXaxDJVBzxYOmrkKig+VfAoGBAMCP\n2Fiehxh5HobsYNmBEuXjHsM0RZiyA0c8LakoPFL8PodUme5PupUw6cNJDJeUUwbQ\nny8vLOK+nMnUKsu6JK5pV/VNsfM3OZU6p5Bf7ylOcEE/sHF1JVWu0CAQO3+3xmx9\n1RPH2mGwHjMhRzPy4jFdP3wi10KgiY+HbLuvEJChAoGAYCsh3UhtTzGUOlPBkmLL\n17bD0wN4J/fOv8BoXPZ8H2CdqVgWy0s+s+QaPqRxNcA6YyGymBqrmQAn1Uii25r9\nKAwVAjg3S2KDEMSI2RbMMmQJSZ1u0GkxqOUC/MMeZqBYTYxVeqcQPoqJZ0Nk7IOA\nZPFif8bVfcZqeimxrFaV6YI=\n-----END PRIVATE KEY-----",
                    "2024-09-09 17:12:03.941339",
                    "https://test-hideout-dev.usbharu.dev/users/a#main-key",
                    "https://test-hideout-dev.usbharu.dev/users/a/following",
                    "https://test-hideout-dev.usbharu.dev/users/a/followers",
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
            insertInto(Instance.tableName) {
                columns(Instance.columns)
                values(
                    1,
                    "system",
                    "",
                    "https://example.com",
                    "",
                    null,
                    "",
                    "",
                    false,
                    false,
                    "",
                    "2024-09-10 16:59:50.160202"
                )
            }
            insertInto("public.media") {
                columns(Media.columns)
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
            insertInto(CustomEmojis.tableName) {
                columns(CustomEmojis.columns)
                values(
                    4,
                    "emoji",
                    "example.com",
                    1,
                    "https://example.com",
                    null,
                    "2024-09-09 17:12:03.941339"
                )
                values(
                    5,
                    "emoji2",
                    "example.com",
                    1,
                    "https://example.com/2",
                    "test",
                    "2024-09-09 17:12:03.941339"
                )
            }
            execute(enableReferenceIntegrityConstraints)
        }.launch()

        val post = TestPostFactory.create(
            id = 1,
            createdAt = Instant.parse("2021-01-01T00:00:00Z"),
            mediaIds = listOf(2, 3),
            emojiIds = listOf(4, 5),
            visibility = Visibility.DIRECT,
            visibleActors = listOf(1, 2)
        )
        repository.save(post)

        assertThat(assertTable)
            .row(0)
            .isEqualTo(Posts.id, post.id.id)
            .isEqualTo(Posts.actorId, post.actorId.id)
            .isEqualTo(Posts.instanceId, post.instanceId.instanceId)
            .isEqualTo(Posts.overview, post.overview?.overview)
            .isEqualTo(Posts.content, post.content.content)
            .isEqualTo(Posts.text, post.text)
            .value(Posts.createdAt).isEqualTo(Timestamp.from(post.createdAt))
            .isEqualTo(Posts.visibility, post.visibility.name)
            .value(Posts.url).isEqualTo(post.url.toString())
            .value(Posts.repostId).isEqualTo(post.repostId?.id)
            .value(Posts.replyId).isEqualTo(post.replyId?.id)
            .isEqualTo(Posts.sensitive, post.sensitive)
            .value(Posts.apId).isEqualTo(post.apId.toString())
            .isEqualTo(Posts.deleted, post.deleted)
            .isEqualTo(Posts.hide, post.hide)
            .value(Posts.moveTo).isEqualTo(post.moveTo?.id)

        assertThat(getTable(PostsMedia.tableName))
            .row(0)
            .value(PostsMedia.postId).isEqualTo(post.id.id)
            .value(PostsMedia.mediaId).isEqualTo(2)
            .row(1)
            .value(PostsMedia.postId).isEqualTo(post.id.id)
            .value(PostsMedia.mediaId).isEqualTo(3)

        assertThat(getTable(PostsEmojis.tableName))
            .row(0)
            .value(PostsEmojis.postId).isEqualTo(post.id.id)
            .value(PostsEmojis.emojiId).isEqualTo(4)
            .row(1)
            .value(PostsEmojis.postId).isEqualTo(post.id.id)
            .value(PostsEmojis.emojiId).isEqualTo(5)

        assertThat(getTable(PostsVisibleActors.tableName))
            .row(0)
            .value(PostsVisibleActors.postId).isEqualTo(post.id.id)
            .value(PostsVisibleActors.actorId).isEqualTo(1)
            .row(1)
            .value(PostsVisibleActors.postId).isEqualTo(post.id.id)
            .value(PostsVisibleActors.actorId).isEqualTo(2)
    }

    @Test
    fun save_idが同じレコードがある場合はupdate() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
            insertInto(Instance.tableName) {
                columns(Instance.columns)
                values(
                    1,
                    "system",
                    "",
                    "https://example.com",
                    "",
                    null,
                    "",
                    "",
                    false,
                    false,
                    "",
                    "2024-09-10 16:59:50.160202"
                )
            }
            insertInto("public.posts") {
                columns(Posts.columns)
                values(
                    1,
                    1832779978794602496,
                    1832779642545639424,
                    null,
                    "<p>test</p>",
                    "test",
                    Timestamp.from(Instant.parse("2020-01-01T00:00:00Z")),
                    "PUBLIC",
                    "http://localhost:8081/users/a/posts/1832779994749734912",
                    2,
                    2,
                    false,
                    "http://localhost:8081/users/a/posts/1832779994749734912",
                    false,
                    false,
                    null
                )
            }
        }.launch()

        val post = TestPostFactory.create(
            id = 1,
            instanceId = 1,
            overview = "aaaaaaaa",
            createdAt = Instant.parse("2021-01-01T00:00:00Z"),
            replyId = 2,
            repostId = 2,
            moveTo = 2
        )

        repository.save(post)

        assertThat(assertTable)
            .row(0)
            .isEqualTo(Posts.id, post.id.id)
            .isEqualTo(Posts.actorId, post.actorId.id)
            .isEqualTo(Posts.instanceId, post.instanceId.instanceId)
            .isEqualTo(Posts.overview, post.overview?.overview)
            .isEqualTo(Posts.content, post.content.content)
            .isEqualTo(Posts.text, post.text)
            .value(Posts.createdAt).isEqualTo(Timestamp.from(post.createdAt))
            .isEqualTo(Posts.visibility, post.visibility.name)
            .value(Posts.url).isEqualTo(post.url.toString())
            .value(Posts.repostId).isEqualTo(post.repostId?.id)
            .value(Posts.replyId).isEqualTo(post.replyId?.id)
            .isEqualTo(Posts.sensitive, post.sensitive)
            .value(Posts.apId).isEqualTo(post.apId.toString())
            .isEqualTo(Posts.deleted, post.deleted)
            .isEqualTo(Posts.hide, post.hide)
            .value(Posts.moveTo).isEqualTo(post.moveTo?.id)
    }

    @Test
    fun findById_指定したIdがある場合は返す() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
            insertInto("public.posts") {
                columns(Posts.columns)
                values(
                    1,
                    1832779978794602496,
                    1832779642545639424,
                    "",
                    "<p>test</p>",
                    "test",
                    Timestamp.from(Instant.parse("2020-01-01T00:00:00Z")),
                    "PUBLIC",
                    "http://localhost:8081/users/a/posts/1832779994749734912",
                    null,
                    null,
                    false,
                    "http://localhost:8081/users/a/posts/1832779994749734912",
                    false,
                    false,
                    null
                )
            }
            insertInto(PostsMedia.tableName) {
                columns(PostsMedia.columns)
                values(1, 2)
            }
            insertInto(PostsEmojis.tableName) {
                columns(PostsEmojis.columns)
                values(1, 3)
            }
            insertInto(PostsVisibleActors.tableName) {
                columns(PostsVisibleActors.columns)
                values(1, 4)
            }
        }.launch()

        val actual = repository.findById(PostId(1))

        val expect = Post(
            id = PostId(1),
            actorId = ActorId(1832779978794602496),
            instanceId = InstanceId(1832779642545639424),
            overview = PostOverview(""),
            content = PostContent("test", "<p>test</p>", listOf(CustomEmojiId(3))),
            createdAt = Instant.parse("2020-01-01T00:00:00Z"),
            visibility = Visibility.PUBLIC,
            url = URI.create("http://localhost:8081/users/a/posts/1832779994749734912"),
            repostId = null,
            replyId = null,
            sensitive = false,
            apId = URI.create("http://localhost:8081/users/a/posts/1832779994749734912"),
            deleted = false,
            mediaIds = listOf(MediaId(2)),
            visibleActors = setOf(ActorId(4)),
            hide = false, moveTo = null
        )

        assertNotNull(actual)
        assertEquals(expect, actual)
        assertEquals(expect.id, actual.id)
        assertEquals(expect.actorId, actual.actorId)
        assertEquals(expect.instanceId, actual.instanceId)
        assertEquals(expect.overview, actual.overview)
        assertEquals(expect.content, actual.content)
        assertEquals(expect.createdAt, actual.createdAt)
        assertEquals(expect.visibility, actual.visibility)
        assertEquals(expect.url, actual.url)
        assertEquals(expect.repostId, actual.repostId)
        assertEquals(expect.replyId, actual.replyId)
        assertEquals(expect.sensitive, actual.sensitive)
        assertEquals(expect.apId, actual.apId)
        assertEquals(expect.deleted, actual.deleted)
        assertEquals(expect.mediaIds, actual.mediaIds)
        assertEquals(expect.visibleActors, actual.visibleActors)
        assertEquals(expect.hide, actual.hide)

    }

    @Test
    fun findById_指定したIdで存在しないとnull() = runTest {
        assertNull(repository.findById(PostId(1)))
    }

    @Test
    fun findAllById_指定されたIdすべて返す() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
            insertInto("public.posts") {
                columns(Posts.columns)
                values(
                    1,
                    1832779978794602496,
                    1832779642545639424,
                    null,
                    "<p>test</p>",
                    "test",
                    Timestamp.from(Instant.parse("2020-01-01T00:00:00Z")),
                    "PUBLIC",
                    "http://localhost:8081/users/a/posts/1832779994749734912",
                    2,
                    2,
                    false,
                    "http://localhost:8081/users/a/posts/1832779994749734912",
                    false,
                    false,
                    null
                )
                values(
                    2,
                    1832779978794602496,
                    1832779642545639424,
                    null,
                    "<p>test</p>",
                    "test",
                    Timestamp.from(Instant.parse("2020-01-01T00:00:00Z")),
                    "PUBLIC",
                    "http://localhost:8081/users/a/posts/18327739994749734912",
                    null,
                    null,
                    false,
                    "http://localhost:8081/users/a/posts/18327793994749734912",
                    false,
                    false,
                    null
                )
                values(
                    3,
                    1832779978794602496,
                    1832779642545639424,
                    "",
                    "<p>test</p>",
                    "test",
                    Timestamp.from(Instant.parse("2020-01-01T00:00:00Z")),
                    "PUBLIC",
                    "http://localhost:8081/users/a/posts/183277399947494734912",
                    null,
                    null,
                    false,
                    "http://localhost:8081/users/a/posts/183277939947493734912",
                    false,
                    false,
                    2
                )
            }
        }.launch()

        val findAllById = repository.findAllById(listOf(PostId(1), PostId(3)))

        assertThat(findAllById)
            .hasSize(2)
    }

    @Test
    fun findByActorId_指定されたActorIdすべて返す() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
            insertInto("public.posts") {
                columns(Posts.columns)
                values(
                    1,
                    1,
                    1832779642545639424,
                    null,
                    "<p>test</p>",
                    "test",
                    Timestamp.from(Instant.parse("2020-01-01T00:00:00Z")),
                    "PUBLIC",
                    "http://localhost:8081/users/a/posts/1832779994749734912",
                    2,
                    2,
                    false,
                    "http://localhost:8081/users/a/posts/1832779994749734912",
                    false,
                    false,
                    null
                )
                values(
                    2,
                    2,
                    1832779642545639424,
                    null,
                    "<p>test</p>",
                    "test",
                    Timestamp.from(Instant.parse("2020-01-01T00:00:00Z")),
                    "PUBLIC",
                    "http://localhost:8081/users/a/posts/18327739994749734912",
                    null,
                    null,
                    false,
                    "http://localhost:8081/users/a/posts/18327793994749734912",
                    false,
                    false,
                    null
                )
                values(
                    3,
                    1,
                    1832779642545639424,
                    "",
                    "<p>test</p>",
                    "test",
                    Timestamp.from(Instant.parse("2020-01-01T00:00:00Z")),
                    "PUBLIC",
                    "http://localhost:8081/users/a/posts/183277399947494734912",
                    null,
                    null,
                    false,
                    "http://localhost:8081/users/a/posts/183277939947493734912",
                    false,
                    false,
                    2
                )
            }
        }.launch()

        val findAllById = repository.findByActorId(ActorId(1))

        assertThat(findAllById)
            .hasSize(2)
    }

    @Test
    fun findByActorId_page_max() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
            insertInto("public.posts") {
                columns(Posts.columns)
                values(
                    1,
                    1,
                    1832779642545639424,
                    null,
                    "<p>test</p>",
                    "test",
                    Timestamp.from(Instant.parse("2020-01-01T00:00:00Z")),
                    "PUBLIC",
                    "http://localhost:8081/users/a/posts/1832779994749734912",
                    2,
                    2,
                    false,
                    "http://localhost:8081/users/a/posts/1832779994749734912",
                    false,
                    false,
                    null
                )
                values(
                    2,
                    1,
                    1832779642545639424,
                    null,
                    "<p>test</p>",
                    "test",
                    Timestamp.from(Instant.parse("2020-01-02T00:00:00Z")),
                    "PUBLIC",
                    "http://localhost:8081/users/a/posts/18327739994749734912",
                    null,
                    null,
                    false,
                    "http://localhost:8081/users/a/posts/18327793994749734912",
                    false,
                    false,
                    null
                )
                values(
                    3,
                    1,
                    1832779642545639424,
                    "",
                    "<p>test</p>",
                    "test",
                    Timestamp.from(Instant.parse("2020-01-03T00:00:00Z")),
                    "PUBLIC",
                    "http://localhost:8081/users/a/posts/183277399947494734912",
                    null,
                    null,
                    false,
                    "http://localhost:8081/users/a/posts/183277939947493734912",
                    false,
                    false,
                    2
                )
            }
        }.launch()

        val findAllById = repository.findByActorId(ActorId(1), Page.of(maxId = 3))

        assertThat(findAllById)
            .hasSize(2)

        assertEquals(2, findAllById[0].id.id)
        assertEquals(1, findAllById[1].id.id)
    }

    @Test
    fun findByActorId_page_min() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
            insertInto("public.posts") {
                columns(Posts.columns)
                values(
                    1,
                    1,
                    1832779642545639424,
                    null,
                    "<p>test</p>",
                    "test",
                    Timestamp.from(Instant.parse("2020-01-01T00:00:00Z")),
                    "PUBLIC",
                    "http://localhost:8081/users/a/posts/1832779994749734912",
                    2,
                    2,
                    false,
                    "http://localhost:8081/users/a/posts/1832779994749734912",
                    false,
                    false,
                    null
                )
                values(
                    2,
                    1,
                    1832779642545639424,
                    null,
                    "<p>test</p>",
                    "test",
                    Timestamp.from(Instant.parse("2020-01-02T00:00:00Z")),
                    "PUBLIC",
                    "http://localhost:8081/users/a/posts/18327739994749734912",
                    null,
                    null,
                    false,
                    "http://localhost:8081/users/a/posts/18327793994749734912",
                    false,
                    false,
                    null
                )
                values(
                    3,
                    1,
                    1832779642545639424,
                    "",
                    "<p>test</p>",
                    "test",
                    Timestamp.from(Instant.parse("2020-01-03T00:00:00Z")),
                    "PUBLIC",
                    "http://localhost:8081/users/a/posts/183277399947494734912",
                    null,
                    null,
                    false,
                    "http://localhost:8081/users/a/posts/183277939947493734912",
                    false,
                    false,
                    2
                )
            }
        }.launch()

        val findAllById = repository.findByActorId(ActorId(1), Page.of(minId = 1))

        assertThat(findAllById)
            .hasSize(2)

        assertEquals(3, findAllById[0].id.id)
        assertEquals(2, findAllById[1].id.id)
    }

    @Test
    fun findByActorId_page_since() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
            insertInto("public.posts") {
                columns(Posts.columns)
                values(
                    1,
                    1,
                    1832779642545639424,
                    null,
                    "<p>test</p>",
                    "test",
                    Timestamp.from(Instant.parse("2020-01-01T00:00:00Z")),
                    "PUBLIC",
                    "http://localhost:8081/users/a/posts/1832779994749734912",
                    2,
                    2,
                    false,
                    "http://localhost:8081/users/a/posts/1832779994749734912",
                    false,
                    false,
                    null
                )
                values(
                    2,
                    1,
                    1832779642545639424,
                    null,
                    "<p>test</p>",
                    "test",
                    Timestamp.from(Instant.parse("2020-01-02T00:00:00Z")),
                    "PUBLIC",
                    "http://localhost:8081/users/a/posts/18327739994749734912",
                    null,
                    null,
                    false,
                    "http://localhost:8081/users/a/posts/18327793994749734912",
                    false,
                    false,
                    null
                )
                values(
                    3,
                    1,
                    1832779642545639424,
                    "",
                    "<p>test</p>",
                    "test",
                    Timestamp.from(Instant.parse("2020-01-03T00:00:00Z")),
                    "PUBLIC",
                    "http://localhost:8081/users/a/posts/183277399947494734912",
                    null,
                    null,
                    false,
                    "http://localhost:8081/users/a/posts/183277939947493734912",
                    false,
                    false,
                    2
                )
            }
        }.launch()

        val findAllById = repository.findByActorId(ActorId(1), Page.of(sinceId = 1))

        assertThat(findAllById)
            .hasSize(2)

        assertEquals(3, findAllById[0].id.id)
        assertEquals(2, findAllById[1].id.id)
    }

    @Test
    fun findByActorId_page_limit() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
            insertInto("public.posts") {
                columns(Posts.columns)
                values(
                    1,
                    1,
                    1832779642545639424,
                    null,
                    "<p>test</p>",
                    "test",
                    Timestamp.from(Instant.parse("2020-01-01T00:00:00Z")),
                    "PUBLIC",
                    "http://localhost:8081/users/a/posts/1832779994749734912",
                    2,
                    2,
                    false,
                    "http://localhost:8081/users/a/posts/1832779994749734912",
                    false,
                    false,
                    null
                )
                values(
                    2,
                    1,
                    1832779642545639424,
                    null,
                    "<p>test</p>",
                    "test",
                    Timestamp.from(Instant.parse("2020-01-02T00:00:00Z")),
                    "PUBLIC",
                    "http://localhost:8081/users/a/posts/18327739994749734912",
                    null,
                    null,
                    false,
                    "http://localhost:8081/users/a/posts/18327793994749734912",
                    false,
                    false,
                    null
                )
                values(
                    3,
                    1,
                    1832779642545639424,
                    "",
                    "<p>test</p>",
                    "test",
                    Timestamp.from(Instant.parse("2020-01-03T00:00:00Z")),
                    "PUBLIC",
                    "http://localhost:8081/users/a/posts/183277399947494734912",
                    null,
                    null,
                    false,
                    "http://localhost:8081/users/a/posts/183277939947493734912",
                    false,
                    false,
                    2
                )
            }
        }.launch()

        val findAllById = repository.findByActorId(ActorId(1), Page.of(limit = 1))

        assertThat(findAllById)
            .hasSize(1)

        assertEquals(3, findAllById[0].id.id)
    }

    @Test
    fun delete_削除される() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
            insertInto("public.posts") {
                columns(Posts.columns)
                values(
                    1,
                    1832779978794602496,
                    1832779642545639424,
                    "",
                    "<p>test</p>",
                    "test",
                    Timestamp.from(Instant.parse("2020-01-01T00:00:00Z")),
                    "PUBLIC",
                    "http://localhost:8081/users/a/posts/1832779994749734912",
                    null,
                    null,
                    false,
                    "http://localhost:8081/users/a/posts/1832779994749734912",
                    false,
                    false,
                    null
                )
            }
            insertInto(PostsMedia.tableName) {
                columns(PostsMedia.columns)
                values(1, 2)
            }
            insertInto(PostsEmojis.tableName) {
                columns(PostsEmojis.columns)
                values(1, 3)
            }
            insertInto(PostsVisibleActors.tableName) {
                columns(PostsVisibleActors.columns)
                values(1, 4)
            }
        }.launch()

        val post = TestPostFactory.create(1)

        val changes = Changes(dataSource)
        changes.withSuspend {
            repository.delete(post)
        }

        assertThat(changes)
            .changeOfDeletionOnTable(Posts.tableName)
            .rowAtStartPoint()
            .value(Posts.id.name).isEqualTo(1)
            .changeOfDeletionOnTable(PostsMedia.tableName)
            .rowAtStartPoint()
            .value(PostsMedia.mediaId.name).isEqualTo(2)
            .changeOfDeletionOnTable(PostsEmojis.tableName)
            .rowAtStartPoint()
            .value(PostsEmojis.emojiId.name).isEqualTo(3)
            .changeOfDeletionOnTable(PostsVisibleActors.tableName)
            .rowAtStartPoint()
            .value(PostsVisibleActors.actorId.name).isEqualTo(4)
    }

    @Test
    fun findByActorIdAndVisiblilityInList_指定されたActorIdかつVisibilityのPostすべて返す() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
            insertInto("public.posts") {
                columns(Posts.columns)
                values(
                    1,
                    1,
                    1832779642545639424,
                    null,
                    "<p>test</p>",
                    "test",
                    Timestamp.from(Instant.parse("2020-01-01T00:00:00Z")),
                    "PUBLIC",
                    "http://localhost:8081/users/a/posts/1832779994749734912",
                    2,
                    2,
                    false,
                    "http://localhost:8081/users/a/posts/1832779994749734912",
                    false,
                    false,
                    null
                )
                values(
                    2,
                    2,
                    1832779642545639424,
                    null,
                    "<p>test</p>",
                    "test",
                    Timestamp.from(Instant.parse("2020-01-02T00:00:00Z")),
                    "PUBLIC",
                    "http://localhost:8081/users/a/posts/18327739994749734912",
                    null,
                    null,
                    false,
                    "http://localhost:8081/users/a/posts/18327793994749734912",
                    false,
                    false,
                    null
                )
                values(
                    3,
                    1,
                    1832779642545639424,
                    "",
                    "<p>test</p>",
                    "test",
                    Timestamp.from(Instant.parse("2020-01-03T00:00:00Z")),
                    "UNLISTED",
                    "http://localhost:8081/users/a/posts/183277399947494734912",
                    null,
                    null,
                    false,
                    "http://localhost:8081/users/a/posts/183277939947493734912",
                    false,
                    false,
                    2
                )
                values(
                    4,
                    1,
                    1832779642545639424,
                    "",
                    "<p>test</p>",
                    "test",
                    Timestamp.from(Instant.parse("2020-01-04T00:00:00Z")),
                    "FOLLOWERS",
                    "http://localhost:8081/users/a/posts/1832773999474947343912",
                    null,
                    null,
                    false,
                    "http://localhost:8081/users/a/posts/1832779399474937349312",
                    false,
                    false,
                    2
                )
            }
        }.launch()

        val findAllById =
            repository.findByActorIdAndVisibilityInList(ActorId(1), listOf(Visibility.PUBLIC, Visibility.FOLLOWERS))

        assertThat(findAllById)
            .hasSize(2)

        assertEquals(4, findAllById[0].id.id)
        assertEquals(1, findAllById[1].id.id)
    }

    @Test
    fun saveAll_idがある場合はinsertなければupdateされる() = runTest {

        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
            insertInto("public.posts") {
                columns(Posts.columns)
                values(
                    1,
                    1,
                    1832779642545639424,
                    null,
                    "<p>test</p>",
                    "test",
                    Timestamp.from(Instant.parse("2020-01-01T00:00:00Z")),
                    "PUBLIC",
                    "http://localhost:8081/users/a/posts/1832779994749734912",
                    2,
                    2,
                    false,
                    "http://localhost:8081/users/a/posts/1832779994749734912",
                    false,
                    false,
                    null
                )
                values(
                    3,
                    1,
                    1832779642545639424,
                    "",
                    "<p>test</p>",
                    "test",
                    Timestamp.from(Instant.parse("2020-01-03T00:00:00Z")),
                    "UNLISTED",
                    "http://localhost:8081/users/a/posts/183277399947494734912",
                    null,
                    null,
                    false,
                    "http://localhost:8081/users/a/posts/183277939947493734912",
                    false,
                    false,
                    2
                )
            }
            insertInto(PostsMedia.tableName) {
                columns(PostsMedia.columns)
                values(1, 1)
            }
            insertInto(PostsEmojis.tableName) {
                columns(PostsEmojis.columns)
                values(1, 2)
            }
            insertInto(PostsVisibleActors.tableName) {
                columns(PostsVisibleActors.columns)
                values(1, 3)
            }
        }.launch()

        val pl = listOf(
            TestPostFactory.create(1, createdAt = Instant.parse("2020-01-04T00:00:00Z"), mediaIds = listOf(5)),
            TestPostFactory.create(2, createdAt = Instant.parse("2020-01-05T00:00:00Z"), emojiIds = listOf(6)),
            TestPostFactory.create(
                3,
                createdAt = Instant.parse("2020-01-06T00:00:00Z"),
                visibleActors = listOf(7),
                overview = "",
                replyId = 1,
                repostId = 1,
                moveTo = 1
            )
        )

        repository.saveAll(pl)

        assertThat(assertTable)
            .row(0)
            .isEqualTo(Posts.id, 1)
            .value(Posts.createdAt).isEqualTo(Timestamp.from(pl[0].createdAt))
            .row(1)
            .isEqualTo(Posts.id, 2)
            .value(Posts.createdAt).isEqualTo(Timestamp.from(pl[1].createdAt))
            .row(2)
            .isEqualTo(Posts.id, 3)
            .value(Posts.createdAt).isEqualTo(Timestamp.from(pl[2].createdAt))

        assertThat(getTable(PostsMedia.tableName))
            .row(0)
            .isEqualTo(PostsMedia.mediaId, 5)

        assertThat(getTable(PostsEmojis.tableName))
            .row(0)
            .isEqualTo(PostsEmojis.emojiId, 6)

        assertThat(getTable(PostsVisibleActors.tableName))
            .row(0)
            .isEqualTo(PostsVisibleActors.actorId, 7)
    }

    @Test
    fun save_ドメインイベントがパブリッシュされる() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
        }.launch()

        val post = TestPostFactory.create()
        post.checkUpdate()
        repository.save(post)

        TransactionManager.current().commit()

        verify(domainEventPublisher, times(1)).publishEvent(any())
    }

    @Test
    fun saveAll_ドメインイベントがすべてパブリッシュされる() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
            insertInto("public.posts") {
                columns(Posts.columns)
                values(
                    1,
                    1,
                    1832779642545639424,
                    null,
                    "<p>test</p>",
                    "test",
                    Timestamp.from(Instant.parse("2020-01-01T00:00:00Z")),
                    "PUBLIC",
                    "http://localhost:8081/users/a/posts/1832779994749734912",
                    2,
                    2,
                    false,
                    "http://localhost:8081/users/a/posts/1832779994749734912",
                    false,
                    false,
                    null
                )
                values(
                    3,
                    1,
                    1832779642545639424,
                    "",
                    "<p>test</p>",
                    "test",
                    Timestamp.from(Instant.parse("2020-01-03T00:00:00Z")),
                    "UNLISTED",
                    "http://localhost:8081/users/a/posts/183277399947494734912",
                    null,
                    null,
                    false,
                    "http://localhost:8081/users/a/posts/183277939947493734912",
                    false,
                    false,
                    2
                )
            }
            insertInto(PostsMedia.tableName) {
                columns(PostsMedia.columns)
                values(1, 1)
            }
            insertInto(PostsEmojis.tableName) {
                columns(PostsEmojis.columns)
                values(1, 2)
            }
            insertInto(PostsVisibleActors.tableName) {
                columns(PostsVisibleActors.columns)
                values(1, 3)
            }
        }.launch()

        val pl = listOf(
            TestPostFactory.create(1, createdAt = Instant.parse("2020-01-04T00:00:00Z"), mediaIds = listOf(5)),
            TestPostFactory.create(2, createdAt = Instant.parse("2020-01-05T00:00:00Z"), emojiIds = listOf(6)),
            TestPostFactory.create(
                3,
                createdAt = Instant.parse("2020-01-06T00:00:00Z"),
                visibleActors = listOf(7),
                overview = "",
                replyId = 1,
                repostId = 1,
                moveTo = 1
            )
        ).map { it.checkUpdate();it }

        repository.saveAll(pl)

        TransactionManager.current().commit()

        verify(domainEventPublisher, times(3)).publishEvent(any())
    }

    @Test
    fun delete_ドメインイベントがパブリッシュされる() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
            insertInto("public.posts") {
                columns(Posts.columns)
                values(
                    1,
                    1,
                    1832779642545639424,
                    null,
                    "<p>test</p>",
                    "test",
                    Timestamp.from(Instant.parse("2020-01-01T00:00:00Z")),
                    "PUBLIC",
                    "http://localhost:8081/users/a/posts/1832779994749734912",
                    2,
                    2,
                    false,
                    "http://localhost:8081/users/a/posts/1832779994749734912",
                    false,
                    false,
                    null
                )

            }
        }.launch()

        val post = TestPostFactory.create(1)
        post.checkUpdate()
        repository.delete(post)

        TransactionManager.current().commit()

        verify(domainEventPublisher, times(1)).publishEvent(any())
    }
}