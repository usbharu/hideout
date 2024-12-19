package dev.usbharu.hideout.core.infrastructure.exposedrepository

import com.ninja_squad.dbsetup_kotlin.dbSetup
import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.emoji.CustomEmojiId
import dev.usbharu.hideout.core.domain.model.emoji.UnicodeEmoji
import dev.usbharu.hideout.core.domain.model.post.PostId
import dev.usbharu.hideout.core.domain.model.reaction.Reaction
import dev.usbharu.hideout.core.domain.model.reaction.ReactionId
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEventPublisher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.db.api.Assertions.assertThat
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import utils.*
import java.sql.Timestamp
import java.time.Instant
import kotlin.test.*

@ExtendWith(MockitoExtension::class)
class ExposedReactionRepositoryTest : AbstractRepositoryTest(Reactions) {

    @InjectMocks
    lateinit var repository: ExposedReactionRepository

    @Mock
    lateinit var domainEventPublisher: DomainEventPublisher


    @Test
    fun save_idが同じレコードがなければinsert() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
            insertInto(Posts.tableName) {
                columns(Posts.columns)
                values(
                    1,
                    1,
                    1,
                    null,
                    "test",
                    "test",
                    Timestamp.from(Instant.parse("2021-01-01T00:00:00Z")),
                    "PUBLIC",
                    "https://example.com",
                    null,
                    null,
                    false,
                    "https://example.com",
                    false,
                    false,
                    null
                )
            }
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
            insertInto(CustomEmojis.tableName) {
                columns(CustomEmojis.columns)
                values(1, "emoji", "example.com", 1, "https://example.com", null, Timestamp.from(Instant.now()))
            }
            execute(enableReferenceIntegrityConstraints)
        }.launch()

        val create = Reaction.create(
            id = ReactionId(1),
            postId = PostId(1),
            actorId = ActorId(1),
            customEmojiId = CustomEmojiId(1),
            unicodeEmoji = UnicodeEmoji("❤"),
            createdAt = Instant.parse("2021-01-01T00:00:00Z")
        )

        repository.save(create)

        assertThat(assertTable)
            .row(0)
            .isEqualTo(Reactions.id, create.id.value)
            .isEqualTo(Reactions.postId, create.postId.id)
            .isEqualTo(Reactions.actorId, create.actorId.id)
            .isEqualTo(Reactions.customEmojiId, create.customEmojiId?.emojiId)
            .isEqualTo(Reactions.unicodeEmoji, create.unicodeEmoji.name)
            .value(Reactions.createdAt).isEqualTo(Timestamp.from(create.createdAt))
    }

    @Test
    fun save_idが同じレコードが存在したらupdate() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
            insertInto(Posts.tableName) {
                columns(Posts.columns)
                values(
                    1,
                    1,
                    1,
                    null,
                    "test",
                    "test",
                    Timestamp.from(Instant.parse("2021-01-01T00:00:00Z")),
                    "PUBLIC",
                    "https://example.com",
                    null,
                    null,
                    false,
                    "https://example.com",
                    false,
                    false,
                    null
                )
            }
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
            insertInto(CustomEmojis.tableName) {
                columns(CustomEmojis.columns)
                values(1, "emoji", "example.com", 1, "https://example.com", null, Timestamp.from(Instant.now()))
            }
            insertInto(Reactions.tableName) {
                columns(Reactions.columns)
                values(1, 1, 1, 2, "☠️", Timestamp.from(Instant.now()))
            }
            execute(enableReferenceIntegrityConstraints)
        }.launch()

        val create = Reaction.create(
            id = ReactionId(1),
            postId = PostId(1),
            actorId = ActorId(1),
            customEmojiId = CustomEmojiId(1),
            unicodeEmoji = UnicodeEmoji("❤"),
            createdAt = Instant.parse("2021-01-01T00:00:00Z")
        )

        repository.save(create)

        assertThat(assertTable)
            .row(0)
            .isEqualTo(Reactions.id, create.id.value)
            .isEqualTo(Reactions.postId, create.postId.id)
            .isEqualTo(Reactions.actorId, create.actorId.id)
            .isEqualTo(Reactions.customEmojiId, create.customEmojiId?.emojiId)
            .isEqualTo(Reactions.unicodeEmoji, create.unicodeEmoji.name)
            .value(Reactions.createdAt).isEqualTo(Timestamp.from(create.createdAt))
    }

    @Test
    fun findById_指定されたIdが存在したら返す() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
            insertInto(Reactions.tableName) {
                columns(Reactions.columns)
                values(1, 1, 1, 1, "❤", Timestamp.from(Instant.parse("2021-01-01T00:00:00Z")))
            }
        }.launch()

        val expected = Reaction.create(
            id = ReactionId(1),
            postId = PostId(1),
            actorId = ActorId(1),
            customEmojiId = CustomEmojiId(1),
            unicodeEmoji = UnicodeEmoji("❤"),
            createdAt = Instant.parse("2021-01-01T00:00:00Z")
        )

        val actual = repository.findById(ReactionId(1))

        assertEquals(expected, actual)

    }

    private fun assertEquals(
        expected: Reaction,
        actual: Reaction?
    ) {
        kotlin.test.assertEquals(expected, actual)
        assertNotNull(actual)
        assertEquals(expected.id, actual.id)
        assertEquals(expected.postId, actual.postId)
        assertEquals(expected.actorId, actual.actorId)
        assertEquals(expected.customEmojiId, actual.customEmojiId)
        assertEquals(expected.unicodeEmoji, actual.unicodeEmoji)
        assertEquals(expected.createdAt, actual.createdAt)
    }

    @Test
    fun findById_指定されたIdがなければnull() = runTest {
        assertNull(repository.findById(ReactionId(1)))
    }

    @Test
    fun findByPostId_指定されたId全部返す() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
            insertInto(Reactions.tableName) {
                columns(Reactions.columns)
                values(1, 1, 1, 1, "❤", Timestamp.from(Instant.parse("2021-01-01T00:00:00Z")))
                values(2, 3, 2, 1, "❤", Timestamp.from(Instant.parse("2021-01-01T00:00:00Z")))
                values(3, 1, 3, 1, "❤", Timestamp.from(Instant.parse("2021-01-01T00:00:00Z")))
            }
        }.launch()

        val actual = repository.findByPostId(PostId(1))

        assertThat(actual)
            .hasSize(2)
    }

    @Test
    fun existsByPostIdAndActorIdAndCustomEmojiIdOrUnicodeEmoji_指定された条件で存在したらtrue() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
            insertInto(Reactions.tableName) {
                columns(Reactions.columns)
                values(1, 1, 1, 1, "❤", Timestamp.from(Instant.parse("2021-01-01T00:00:00Z")))
                values(2, 3, 2, null, "❤", Timestamp.from(Instant.parse("2021-01-01T00:00:00Z")))
                values(3, 1, 3, 1, "❤", Timestamp.from(Instant.parse("2021-01-01T00:00:00Z")))
            }
        }.launch()

        val actual1 = repository.existsByPostIdAndActorIdAndCustomEmojiIdOrUnicodeEmoji(
            PostId(1),
            ActorId(1), CustomEmojiId
                (1), "❤"
        )
        val actual2 = repository.existsByPostIdAndActorIdAndCustomEmojiIdOrUnicodeEmoji(
            PostId(3),
            ActorId(2), null, "❤"
        )

        assertTrue(actual1)
        assertTrue(actual2)
    }

    @Test
    fun existsByPostIdAndActorIdAndCustomEmojiIdOrUnicodeEmoji_指定された条件で存在しないとfalse() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
            insertInto(Reactions.tableName) {
                columns(Reactions.columns)
                values(1, 1, 1, 1, "❤", Timestamp.from(Instant.parse("2021-01-01T00:00:00Z")))
                values(2, 3, 2, null, "❤", Timestamp.from(Instant.parse("2021-01-01T00:00:00Z")))
                values(3, 1, 3, 1, "❤", Timestamp.from(Instant.parse("2021-01-01T00:00:00Z")))
            }
        }.launch()

        val actual1 = repository.existsByPostIdAndActorIdAndCustomEmojiIdOrUnicodeEmoji(
            PostId(3),
            ActorId(1), CustomEmojiId
                (1), "❤"
        )
        val actual2 = repository.existsByPostIdAndActorIdAndCustomEmojiIdOrUnicodeEmoji(
            PostId(2),
            ActorId(2), null, "❤"
        )

        assertFalse(actual1)
        assertFalse(actual2)
    }

    @Test
    fun delete_削除される() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
            insertInto(Reactions.tableName) {
                columns(Reactions.columns)
                values(1, 1, 1, 1, "❤", Timestamp.from(Instant.parse("2021-01-01T00:00:00Z")))
                values(2, 3, 2, null, "❤", Timestamp.from(Instant.parse("2021-01-01T00:00:00Z")))
                values(3, 1, 3, 1, "❤", Timestamp.from(Instant.parse("2021-01-01T00:00:00Z")))
            }
        }.launch()

        val reaction = Reaction(
            ReactionId(1),
            PostId(1),
            ActorId(1),
            CustomEmojiId(1),
            UnicodeEmoji("❤"),
            Instant.parse("2021-01-01T00:00:00Z")
        )

        repository.delete(reaction)
    }

    @Test
    fun findByPostIdAndActorIdAndCustomEmojiIdOrUnicodeEmoji_指定された条件で存在したら返す() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
            insertInto(Reactions.tableName) {
                columns(Reactions.columns)
                values(1, 1, 1, 1, "❤", Timestamp.from(Instant.parse("2021-01-01T00:00:00Z")))
                values(2, 3, 2, null, "❤", Timestamp.from(Instant.parse("2021-01-01T00:00:00Z")))
                values(3, 1, 3, 1, "❤", Timestamp.from(Instant.parse("2021-01-01T00:00:00Z")))
            }
        }.launch()

        val expected = Reaction(
            ReactionId(1),
            PostId(1),
            ActorId(1),
            CustomEmojiId(1),
            UnicodeEmoji("❤"),
            Instant.parse("2021-01-01T00:00:00Z")
        )

        val actual =
            repository.findByPostIdAndActorIdAndCustomEmojiIdOrUnicodeEmoji(
                PostId(1),
                ActorId(1), CustomEmojiId
                    (1), "❤"
            )

        assertEquals(expected, actual)
    }

    @Test
    fun findByPostIdAndActorIdAndCustomEmojiIdOrUnicodeEmoji_指定された条件で存在しないとnull() = runTest {
        assertNull(
            repository.findByPostIdAndActorIdAndCustomEmojiIdOrUnicodeEmoji(
                PostId(1),
                ActorId(1), CustomEmojiId
                    (1), "❤"
            )
        )
    }

    @Test
    fun save_ドメインイベントがパブリッシュされる() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
        }.launch()
        repository.save(
            Reaction.create(
                ReactionId(1),
                PostId(1), ActorId
                    (1), CustomEmojiId
                    (1), UnicodeEmoji("❤"), Instant.now
                    ()
            )
        )

        TransactionManager.current().commit()

        verify(domainEventPublisher, times(1)).publishEvent(any())
    }

    @Test
    fun delete_ドメインイベントがパブリッシュされる() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
            insertInto(Reactions.tableName) {
                columns(Reactions.columns)
                values(1, 1, 1, 1, "❤", Timestamp.from(Instant.parse("2021-01-01T00:00:00Z")))
                values(2, 3, 2, null, "❤", Timestamp.from(Instant.parse("2021-01-01T00:00:00Z")))
                values(3, 1, 3, 1, "❤", Timestamp.from(Instant.parse("2021-01-01T00:00:00Z")))
            }
        }.launch()

        val reaction = Reaction(
            ReactionId(1),
            PostId(1), ActorId
                (1), CustomEmojiId
                (1), UnicodeEmoji("❤"), Instant.now
                ()
        )
        reaction.delete()
        repository.delete(
            reaction
        )

        TransactionManager.current().commit()

        verify(domainEventPublisher, times(1)).publishEvent(any())
    }
}