package dev.usbharu.hideout.core.infrastructure.exposedrepository

import com.ninja_squad.dbsetup_kotlin.dbSetup
import dev.usbharu.hideout.core.domain.model.filter.*
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailId
import dev.usbharu.hideout.core.infrastructure.exposed.FilterQueryMapper
import dev.usbharu.hideout.core.infrastructure.exposed.FilterResultRowMapper
import kotlinx.coroutines.test.runTest
import org.assertj.db.api.Assertions.assertThat
import org.assertj.db.type.Changes
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import utils.*
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@ExtendWith(MockitoExtension::class)
class ExposedFilterRepositoryTest : AbstractRepositoryTest(Filters) {

    @InjectMocks
    lateinit var repository: ExposedFilterRepository

    @Spy
    val filterQueryMapper: FilterQueryMapper = FilterQueryMapper(FilterResultRowMapper())

    @Test
    fun save_idが同じレコードが存在しないとinsert() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
            insertInto(UserDetails.tableName) {
                columns(UserDetails.columns)
                values(1, 2, "VeeeeeeeeeeeeeeeeryStrongPassword", false, null, null)
            }
            execute(enableReferenceIntegrityConstraints)
        }.launch()

        val filter = Filter(
            FilterId(1),
            UserDetailId(1),
            FilterName("filter"),
            setOf(),
            FilterAction.HIDE,
            setOf(FilterKeyword(FilterKeywordId(1), FilterKeywordKeyword("keyword"), FilterMode.NONE))
        )
        repository.save(filter)

        assertThat(assertTable).row(0).isEqualTo(Filters.id, filter.id.id)
            .isEqualTo(Filters.userId, filter.userDetailId.id).isEqualTo(Filters.name, filter.name.name)
            .isEqualTo(Filters.filterAction, filter.filterAction.name)
            .isEqualTo(Filters.context, filter.filterContext.joinToString(",") { it.name })

        assertThat(getTable(FilterKeywords.tableName)).row(0)
            .isEqualTo(FilterKeywords.id, filter.filterKeywords.first().id.id)
            .isEqualTo(FilterKeywords.filterId, filter.id.id)
            .isEqualTo(FilterKeywords.keyword, filter.filterKeywords.first().keyword.keyword)
            .isEqualTo(FilterKeywords.mode, filter.filterKeywords.first().mode.name)
    }

    @Test
    fun save_idが同じレコードが存在したらupdate() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
            insertInto(UserDetails.tableName) {
                columns(UserDetails.columns)
                values(1, 2, "VeeeeeeeeeeeeeeeeryStrongPassword", false, null, null)
            }
            insertInto(Filters.tableName) {
                columns(Filters.columns)
                values(1, 1, "name", "", "WARN")
            }
            insertInto(FilterKeywords.tableName) {
                columns(FilterKeywords.columns)
                values(1, 1, "aaaaaaaaaaaaaaaaa", "REGEX")
            }
            execute(enableReferenceIntegrityConstraints)
        }.launch()

        val filter = Filter(
            FilterId(1),
            UserDetailId(1),
            FilterName("filter"),
            setOf(),
            FilterAction.HIDE,
            setOf(FilterKeyword(FilterKeywordId(1), FilterKeywordKeyword("keyword"), FilterMode.NONE))
        )
        repository.save(filter)

        assertThat(assertTable).row(0).isEqualTo(Filters.id, filter.id.id)
            .isEqualTo(Filters.userId, filter.userDetailId.id).isEqualTo(Filters.name, filter.name.name)
            .isEqualTo(Filters.filterAction, filter.filterAction.name)
            .isEqualTo(Filters.context, filter.filterContext.joinToString(",") { it.name })

        assertThat(getTable(FilterKeywords.tableName)).row(0)
            .isEqualTo(FilterKeywords.id, filter.filterKeywords.first().id.id)
            .isEqualTo(FilterKeywords.filterId, filter.id.id)
            .isEqualTo(FilterKeywords.keyword, filter.filterKeywords.first().keyword.keyword)
            .isEqualTo(FilterKeywords.mode, filter.filterKeywords.first().mode.name)
    }

    @Test
    fun delete_削除される() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
            insertInto(UserDetails.tableName) {
                columns(UserDetails.columns)
                values(1, 2, "VeeeeeeeeeeeeeeeeryStrongPassword", false, null, null)
            }
            insertInto(Filters.tableName) {
                columns(Filters.columns)
                values(1, 1, "name", "", "WARN")
            }
            insertInto(FilterKeywords.tableName) {
                columns(FilterKeywords.columns)
                values(1, 1, "aaaaaaaaaaaaaaaaa", "REGEX")
            }
            execute(enableReferenceIntegrityConstraints)
        }.launch()

        val filter = Filter(
            FilterId(1),
            UserDetailId(1),
            FilterName("filter"),
            setOf(),
            FilterAction.HIDE,
            setOf(FilterKeyword(FilterKeywordId(1), FilterKeywordKeyword("keyword"), FilterMode.NONE))
        )

        val changes = Changes(dataSource)
        changes.withSuspend {
            repository.delete(filter)
        }

        assertThat(changes).changeOfDeletionOnTable(Filters.tableName).rowAtStartPoint().value(Filters.id.name)
            .isEqualTo(filter.id.id).changeOfDeletionOnTable(FilterKeywords.tableName).rowAtStartPoint()
            .value(FilterKeywords.id.name).isEqualTo(filter.filterKeywords.first().id.id)
    }

    @Test
    fun findByFilterKeywordId_指定された条件で存在したら返す() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)

            insertInto(Filters.tableName) {
                columns(Filters.columns)
                values(1, 1, "name", "PUBLIC", "WARN")
            }
            insertInto(FilterKeywords.tableName) {
                columns(FilterKeywords.columns)
                values(1, 1, "keyword", "REGEX")
            }

            execute(enableReferenceIntegrityConstraints)
        }.launch()

        val expected = Filter(
            FilterId(1),
            UserDetailId(1),
            FilterName("name"),
            setOf(FilterContext.PUBLIC),
            FilterAction.WARN,
            setOf(FilterKeyword(FilterKeywordId(1), FilterKeywordKeyword("keyword"), FilterMode.REGEX))
        )

        val actual = repository.findByFilterKeywordId(FilterKeywordId(1))

        assertEquals(expected, actual)
    }

    private fun assertEquals(
        expected: Filter, actual: Filter?
    ) {
        kotlin.test.assertEquals(expected, actual)
        assertNotNull(actual)
        assertEquals(expected.id, actual.id)
        assertEquals(expected.name, actual.name)
        assertContentEquals(expected.filterContext, actual.filterContext.asIterable())
        assertEquals(expected.userDetailId, actual.userDetailId)
        assertEquals(expected.filterKeywords.size, actual.filterKeywords.size)
        assertContentEquals(expected.filterKeywords, actual.filterKeywords.asIterable())
    }


    @Test
    fun findByFilterKeywordId_指定された条件で存在しないとnull() = runTest {
        assertNull(repository.findByFilterKeywordId(FilterKeywordId(1)))
    }

    @Test
    fun findByFilterId_指定された条件で存在したら返す() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)

            insertInto(Filters.tableName) {
                columns(Filters.columns)
                values(1, 1, "name", "PUBLIC", "WARN")
            }
            insertInto(FilterKeywords.tableName) {
                columns(FilterKeywords.columns)
                values(1, 1, "keyword", "REGEX")
            }

            execute(enableReferenceIntegrityConstraints)
        }.launch()

        val expected = Filter(
            FilterId(1),
            UserDetailId(1),
            FilterName("name"),
            setOf(FilterContext.PUBLIC),
            FilterAction.WARN,
            setOf(FilterKeyword(FilterKeywordId(1), FilterKeywordKeyword("keyword"), FilterMode.REGEX))
        )

        val actual = repository.findByFilterId(FilterId(1))

        assertEquals(expected, actual)
    }

    @Test
    fun findByFilterId_指定された条件で存在しないとnull() = runTest {
        assertNull(repository.findByFilterId(FilterId(1)))
    }

    @Test
    fun findByUserDetailId_指定された条件全部返す() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)

            insertInto(Filters.tableName) {
                columns(Filters.columns)
                values(1, 1, "name", "PUBLIC", "WARN")
                values(2, 1, "name2", "PUBLIC", "WARN")
                values(3, 1, "name3", "PUBLIC", "HIDE")
            }
            insertInto(FilterKeywords.tableName) {
                columns(FilterKeywords.columns)
                values(1, 1, "keyword", "REGEX")
                values(2, 2, "keyword2", "REGEX")
                values(3, 1, "keyword3", "REGEX")
            }

            execute(enableReferenceIntegrityConstraints)
        }.launch()

        val expected = listOf(
            Filter(
                FilterId(1), UserDetailId(1), FilterName("name"), setOf(FilterContext.PUBLIC), FilterAction.WARN, setOf(
                    FilterKeyword(FilterKeywordId(1), FilterKeywordKeyword("keyword"), FilterMode.REGEX),
                    FilterKeyword(FilterKeywordId(3), FilterKeywordKeyword("keyword3"), FilterMode.REGEX)
                )
            ), Filter(
                FilterId(2),
                UserDetailId(1),
                FilterName("name2"),
                setOf(FilterContext.PUBLIC),
                FilterAction.WARN,
                setOf(
                    FilterKeyword(FilterKeywordId(2), FilterKeywordKeyword("keyword2"), FilterMode.REGEX)
                )
            ), Filter(
                FilterId(3),
                UserDetailId(1),
                FilterName("name3"),
                setOf(FilterContext.PUBLIC),
                FilterAction.HIDE,
                setOf(
                )
            )
        )

        val actual = repository.findByUserDetailId(UserDetailId(1))

        assertContentEquals(expected, actual)
    }
}