package dev.usbharu.hideout.core.infrastructure.exposedrepository

import com.ninja_squad.dbsetup_kotlin.dbSetup
import dev.usbharu.hideout.core.domain.model.emoji.CustomEmoji
import dev.usbharu.hideout.core.domain.model.emoji.CustomEmojiId
import dev.usbharu.hideout.core.domain.model.instance.InstanceId
import dev.usbharu.hideout.core.domain.model.support.domain.Domain
import kotlinx.coroutines.test.runTest
import org.assertj.db.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import utils.*
import java.net.URI
import java.sql.Timestamp
import java.time.Instant
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ExposedCustomEmojiRepositoryTest : AbstractRepositoryTest(CustomEmojis) {
    @Test
    fun save_idが同じレコードが存在しないとinsert() = runTest {
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
            execute(enableReferenceIntegrityConstraints)
        }.launch()
        val customEmoji = CustomEmoji(
            CustomEmojiId(1),
            "name",
            Domain("example.com"),
            InstanceId(1),
            URI.create("https://example.com"),
            null,
            Instant.parse("2020-01-01T00:00:00Z")
        )

        ExposedCustomEmojiRepository().save(customEmoji)

        assertThat(assertTable).row(0).isEqualTo(CustomEmojis.id, customEmoji.id.emojiId)
            .isEqualTo(CustomEmojis.name, customEmoji.name).isEqualTo(CustomEmojis.domain, customEmoji.domain.domain)
            .isEqualTo(CustomEmojis.instanceId, customEmoji.instanceId.instanceId)
            .isEqualTo(CustomEmojis.url, customEmoji.url.toString())
            .isEqualTo(CustomEmojis.category, customEmoji.category).value(CustomEmojis.createdAt.name)
            .isEqualTo(Timestamp.from(customEmoji.createdAt))
    }

    @Test
    fun save_idが同じレコードが存在したらupdate() = runTest {
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
            insertInto(CustomEmojis.tableName) {
                columns(CustomEmojis.columns)
                values(
                    1,
                    "emoji",
                    "example.com",
                    1,
                    "https://example.com",
                    null,
                    Timestamp.from(Instant.parse("2020-01-01T00:00:00Z"))
                )
            }
            execute(enableReferenceIntegrityConstraints)
        }.launch()
        val customEmoji = CustomEmoji(
            CustomEmojiId(1),
            "name",
            Domain("example.com"),
            InstanceId(1),
            URI.create("https://example.com"),
            null,
            Instant.parse("2020-01-01T00:00:00Z")
        )

        ExposedCustomEmojiRepository().save(customEmoji)

        assertThat(assertTable).row(0).isEqualTo(CustomEmojis.id, customEmoji.id.emojiId)
            .isEqualTo(CustomEmojis.name, customEmoji.name).isEqualTo(CustomEmojis.domain, customEmoji.domain.domain)
            .isEqualTo(CustomEmojis.instanceId, customEmoji.instanceId.instanceId)
            .isEqualTo(CustomEmojis.url, customEmoji.url.toString())
            .isEqualTo(CustomEmojis.category, customEmoji.category).value(CustomEmojis.createdAt.name)
            .isEqualTo(Timestamp.from(customEmoji.createdAt))
    }

    @Test
    fun delete_削除される() = runTest {

        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
            insertInto(CustomEmojis.tableName) {
                columns(CustomEmojis.columns)
                values(
                    1,
                    "emoji",
                    "example.com",
                    1,
                    "https://example.com",
                    null,
                    Timestamp.from(Instant.parse("2020-01-01T00:00:00Z"))
                )
            }
        }.launch()

        val customEmoji = CustomEmoji(
            CustomEmojiId(1),
            "name",
            Domain("example.com"),
            InstanceId(1),
            URI.create("https://example.com"),
            null,
            Instant.parse("2020-01-01T00:00:00Z")
        )


        change.withSuspend {
            ExposedCustomEmojiRepository().delete(customEmoji)
        }

        assertThat(change).changeOfDeletionOnTable(CustomEmojis.tableName).rowAtStartPoint().value(CustomEmojis.id.name)
            .isEqualTo(customEmoji.id.emojiId)
    }

    @Test
    fun findById_指定したIdで存在したら返す() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
            insertInto(CustomEmojis.tableName) {
                columns(CustomEmojis.columns)
                values(
                    1,
                    "emoji",
                    "example.com",
                    1,
                    "https://example.com",
                    null,
                    Timestamp.from(Instant.parse("2020-01-01T00:00:00Z"))
                )
            }
        }.launch()

        val customEmoji = CustomEmoji(
            CustomEmojiId(1),
            "emoji",
            Domain("example.com"),
            InstanceId(1),
            URI.create("https://example.com"),
            null,
            Instant.parse("2020-01-01T00:00:00Z")
        )

        val actual = ExposedCustomEmojiRepository().findById(1)

        assertEquals(customEmoji, actual)
        assertNotNull(actual)
        assertEquals(customEmoji.id, actual.id)
        assertEquals(customEmoji.createdAt, actual.createdAt)
        assertEquals(customEmoji.url, actual.url)
        assertEquals(customEmoji.category, actual.category)
        assertEquals(customEmoji.instanceId, actual.instanceId)
        assertEquals(customEmoji.domain, actual.domain)
        assertEquals(customEmoji.name, actual.name)
    }

    @Test
    fun findById_指定したIdで存在しないとnull() = runTest {
        assertNull(ExposedCustomEmojiRepository().findById(1))
    }

    @Test
    fun findByNamesAndDomain_指定した条件全部返す() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
            insertInto(CustomEmojis.tableName) {
                columns(CustomEmojis.columns)
                values(
                    1,
                    "emoji",
                    "example.com",
                    1,
                    "https://example.com/1",
                    null,
                    Timestamp.from(Instant.parse("2020-01-01T00:00:00Z"))
                )
                values(
                    2,
                    "emoji2",
                    "example.com",
                    1,
                    "https://example.com/2",
                    null,
                    Timestamp.from(Instant.parse("2020-01-01T00:00:00Z"))
                )
                values(
                    3,
                    "emoji3",
                    "example.com",
                    1,
                    "https://example.com/3",
                    null,
                    Timestamp.from(Instant.parse("2020-01-01T00:00:00Z"))
                )
            }
        }.launch()

        val expected = listOf(
            CustomEmoji(
                CustomEmojiId(1),
                "emoji",
                Domain("example.com"),
                InstanceId(1),
                URI.create("https://example.com/1"),
                null,
                Instant.parse("2020-01-01T00:00:00Z")
            ), CustomEmoji(
                CustomEmojiId(2),
                "emoji2",
                Domain("example.com"),
                InstanceId(1),
                URI.create("https://example.com/2"),
                null,
                Instant.parse("2020-01-01T00:00:00Z")
            )
        )

        val actual = ExposedCustomEmojiRepository().findByNamesAndDomain(listOf("emoji", "emoji2"), "example.com")

        assertContentEquals(expected, actual)
    }

    @Test
    fun findByIds_指定された条件全部返す() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
            insertInto(CustomEmojis.tableName) {
                columns(CustomEmojis.columns)
                values(
                    1,
                    "emoji",
                    "example.com",
                    1,
                    "https://example.com/1",
                    null,
                    Timestamp.from(Instant.parse("2020-01-01T00:00:00Z"))
                )
                values(
                    2,
                    "emoji2",
                    "example.com",
                    1,
                    "https://example.com/2",
                    null,
                    Timestamp.from(Instant.parse("2020-01-01T00:00:00Z"))
                )
                values(
                    3,
                    "emoji3",
                    "example.com",
                    1,
                    "https://example.com/3",
                    null,
                    Timestamp.from(Instant.parse("2020-01-01T00:00:00Z"))
                )
            }
        }.launch()

        val expected = listOf(
            CustomEmoji(
                CustomEmojiId(1),
                "emoji",
                Domain("example.com"),
                InstanceId(1),
                URI.create("https://example.com/1"),
                null,
                Instant.parse("2020-01-01T00:00:00Z")
            ), CustomEmoji(
                CustomEmojiId(3),
                "emoji3",
                Domain("example.com"),
                InstanceId(1),
                URI.create("https://example.com/3"),
                null,
                Instant.parse("2020-01-01T00:00:00Z")
            )
        )

        val actual = ExposedCustomEmojiRepository().findByIds(listOf(1, 3))

        assertContentEquals(expected, actual)
    }
}