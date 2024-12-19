package dev.usbharu.hideout.core.infrastructure.exposedrepository

import com.ninja_squad.dbsetup_kotlin.dbSetup
import dev.usbharu.hideout.core.domain.model.instance.*
import dev.usbharu.hideout.core.domain.model.instance.Instance
import kotlinx.coroutines.test.runTest
import org.assertj.db.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import utils.AbstractRepositoryTest
import utils.columns
import utils.isEqualTo
import utils.value
import java.net.URI
import java.sql.Timestamp
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import dev.usbharu.hideout.core.infrastructure.exposedrepository.Instance as InstanceTable

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ExposedInstanceRepositoryTest : AbstractRepositoryTest(InstanceTable) {


    @Test
    fun save_idが同じレコードがない場合はinsertされる() = runTest {
        ExposedInstanceRepository().save(
            Instance(
                id = InstanceId(1),
                name = InstanceName("test"),
                description = InstanceDescription("id"),
                url = URI.create("https://www.example.com"),
                iconUrl = URI.create("https://www.example.com"),
                sharedInbox = null,
                software = InstanceSoftware(""),
                version = InstanceVersion(""),
                isBlocked = false,
                isMuted = false,
                moderationNote = InstanceModerationNote(""),
                createdAt = Instant.parse("2020-01-01T00:00:00Z"),
            )
        )

        val table = assertTable
        assertThat(table).row(1).isEqualTo(InstanceTable.id, 1).isEqualTo(InstanceTable.name, "test")
            .value(InstanceTable.url).isEqualTo("https://www.example.com")
            .value(InstanceTable.iconUrl).isEqualTo("https://www.example.com")
            .isEqualTo(InstanceTable.sharedInbox, null)
            .isEqualTo(InstanceTable.software, "").isEqualTo(InstanceTable.version, "")
            .isEqualTo(InstanceTable.isBlocked, false).isEqualTo(InstanceTable.isMuted, false)
            .isEqualTo(InstanceTable.moderationNote, "").value(InstanceTable.createdAt)
            .isEqualTo(Timestamp.from(Instant.parse("2020-01-01T00:00:00Z")))
    }

    @Test
    fun save_idが同じレコードがある場合はupdateされる() = runTest {
        dbSetup(to = dataSource) {
            insertInto(InstanceTable.tableName) {
                columns(
                    InstanceTable.columns
                )
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
        }.launch()


        ExposedInstanceRepository().save(
            Instance(
                id = InstanceId(1),
                name = InstanceName("test"),
                description = InstanceDescription("id"),
                url = URI.create("https://www.example.com"),
                iconUrl = URI.create("https://www.example.com"),
                sharedInbox = null,
                software = InstanceSoftware(""),
                version = InstanceVersion(""),
                isBlocked = false,
                isMuted = false,
                moderationNote = InstanceModerationNote(""),
                createdAt = Instant.parse("2020-01-01T00:00:00Z"),
            )
        )

        val table = assertTable
        assertThat(table).row(1).isEqualTo(InstanceTable.id, 1).isEqualTo(InstanceTable.name, "test")
            .value(InstanceTable.url).isEqualTo("https://www.example.com")
            .value(InstanceTable.iconUrl).isEqualTo("https://www.example.com")
            .isEqualTo(InstanceTable.sharedInbox, null)
            .isEqualTo(InstanceTable.software, "").isEqualTo(InstanceTable.version, "")
            .isEqualTo(InstanceTable.isBlocked, false).isEqualTo(InstanceTable.isMuted, false)
            .isEqualTo(InstanceTable.moderationNote, "").value(InstanceTable.createdAt)
            .isEqualTo(Timestamp.from(Instant.parse("2020-01-01T00:00:00Z")))
    }

    @Test
    fun findById_指定したidで存在したら返す() = runTest {
        dbSetup(to = dataSource) {
            insertInto(InstanceTable.tableName) {
                columns(
                    InstanceTable.columns
                )
                values(
                    1,
                    "test",
                    "description",
                    "https://www.example.com",
                    "https://www.example.com",
                    null,
                    "",
                    "",
                    false,
                    false,
                    "",
                    Timestamp.from(Instant.parse("2020-01-01T00:00:00Z"))
                )
            }
        }.launch()

        val actual = ExposedInstanceRepository().findById(InstanceId(1))
        val expected = Instance(
            id = InstanceId(1),
            name = InstanceName("test"),
            description = InstanceDescription("description"),
            url = URI.create("https://www.example.com"),
            iconUrl = URI.create("https://www.example.com"),
            sharedInbox = null,
            software = InstanceSoftware(""),
            version = InstanceVersion(""),
            isBlocked = false,
            isMuted = false,
            moderationNote = InstanceModerationNote(""),
            createdAt = Instant.parse("2020-01-01T00:00:00Z"),
        )

        assertEquals(expected, actual)
    }

    @Test
    fun findById_指定したIDで存在しないとnull() = runTest {
        assertNull(ExposedInstanceRepository().findById(InstanceId(1)))
    }

    @Test
    fun findByUrl_指定したURLで存在したら返す() = runTest {
        dbSetup(to = dataSource) {
            insertInto(InstanceTable.tableName) {
                columns(
                    InstanceTable.columns
                )
                values(
                    1,
                    "test",
                    "description",
                    "https://www.example.com",
                    "https://www.example.com",
                    null,
                    "",
                    "",
                    false,
                    false,
                    "",
                    Timestamp.from(Instant.parse("2020-01-01T00:00:00Z"))
                )
            }
        }.launch()

        val actual = ExposedInstanceRepository().findByUrl(URI.create("https://www.example.com"))
        val expected = Instance(
            id = InstanceId(1),
            name = InstanceName("test"),
            description = InstanceDescription("description"),
            url = URI.create("https://www.example.com"),
            iconUrl = URI.create("https://www.example.com"),
            sharedInbox = null,
            software = InstanceSoftware(""),
            version = InstanceVersion(""),
            isBlocked = false,
            isMuted = false,
            moderationNote = InstanceModerationNote(""),
            createdAt = Instant.parse("2020-01-01T00:00:00Z"),
        )

        assertEquals(expected, actual)
    }

    @Test
    fun findByUrl_指定したURLで存在しないとnull() = runTest {
        assertNull(ExposedInstanceRepository().findByUrl(URI.create("https://www.example.com")))
    }

    @Test
    fun delete_削除() = runTest {
        dbSetup(to = dataSource) {
            insertInto(InstanceTable.tableName) {
                columns(
                    InstanceTable.columns
                )
                values(
                    1,
                    "test",
                    "description",
                    "https://www.example.com",
                    "https://www.example.com",
                    null,
                    "",
                    "",
                    false,
                    false,
                    "",
                    Timestamp.from(Instant.parse("2020-01-01T00:00:00Z"))
                )
            }
        }.launch()

        val instance = Instance(
            id = InstanceId(1),
            name = InstanceName("test"),
            description = InstanceDescription("description"),
            url = URI.create("https://www.example.com"),
            iconUrl = URI.create("https://www.example.com"),
            sharedInbox = null,
            software = InstanceSoftware(""),
            version = InstanceVersion(""),
            isBlocked = false,
            isMuted = false,
            moderationNote = InstanceModerationNote(""),
            createdAt = Instant.parse("2020-01-01T00:00:00Z"),
        )

        change.setStartPointNow()

        ExposedInstanceRepository().delete(instance)

        change.setEndPointNow()

        assertThat(change)
            .hasNumberOfChanges(1)
            .changeOfDeletionOnTable(InstanceTable.tableName)
            .rowAtStartPoint()
            .value(InstanceTable.id.name).isEqualTo(1)

    }

    companion object {
        fun assertEquals(expected: Instance, actual: Instance?) {
            assertNotNull(actual)
            kotlin.test.assertEquals(expected, actual)
            assertEquals(expected.name, actual.name)
            assertEquals(expected.description, actual.description)
            assertEquals(expected.url, actual.url)
            assertEquals(expected.iconUrl, actual.iconUrl)
            assertEquals(expected.sharedInbox, actual.sharedInbox)
            assertEquals(expected.software, actual.software)
            assertEquals(expected.version, actual.version)
            assertEquals(expected.isBlocked, actual.isBlocked)
            assertEquals(expected.moderationNote, actual.moderationNote)
            assertEquals(expected.createdAt, actual.createdAt)
        }
    }
}

