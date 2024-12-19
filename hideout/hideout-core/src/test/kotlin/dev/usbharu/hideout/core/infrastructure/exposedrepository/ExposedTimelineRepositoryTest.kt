package dev.usbharu.hideout.core.infrastructure.exposedrepository

import com.ninja_squad.dbsetup_kotlin.dbSetup
import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.timeline.Timeline
import dev.usbharu.hideout.core.domain.model.timeline.TimelineId
import dev.usbharu.hideout.core.domain.model.timeline.TimelineName
import dev.usbharu.hideout.core.domain.model.timeline.TimelineVisibility
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetail
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailHashedPassword
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailId
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
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@ExtendWith(MockitoExtension::class)
class ExposedTimelineRepositoryTest : AbstractRepositoryTest(Timelines) {
    @InjectMocks
    lateinit var repository: ExposedTimelineRepository

    @Mock
    lateinit var domainEventPublisher: DomainEventPublisher

    @Test
    fun save_idが同じレコードが存在しない場合insert() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
            insertInto(UserDetails.tableName) {
                columns(UserDetails.columns)
                values(1, 1, "veeeeeeeeeeeeeeryStrongPassword", true, null, null)
            }
        }.launch()
        val timeline = Timeline(
            id = TimelineId(1),
            userDetailId = UserDetailId(1),
            name = TimelineName("timeline"),
            visibility = TimelineVisibility.PUBLIC,
            isSystem = false
        )

        repository.save(timeline)

        assertThat(assertTable).row(0).isEqualTo(Timelines.id, timeline.id.value)
            .isEqualTo(Timelines.userDetailId, timeline.userDetailId.id).isEqualTo(Timelines.name, timeline.name.value)
            .isEqualTo(Timelines.visibility, timeline.visibility.name).isEqualTo(Timelines.isSystem, timeline.isSystem)
    }

    @Test
    fun save_idが同じレコードが存在する場合update() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
            insertInto(UserDetails.tableName) {
                columns(UserDetails.columns)
                values(1, 1, "veeeeeeeeeeeeeeryStrongPassword", true, null, null)
            }
            insertInto(Timelines.tableName) {
                columns(Timelines.columns)
                values(1, 1, "test-timeline", "PUBLIC", true)
            }

        }.launch()
        val timeline = Timeline(
            id = TimelineId(1),
            userDetailId = UserDetailId(1),
            name = TimelineName("timeline"),
            visibility = TimelineVisibility.PRIVATE,
            isSystem = false
        )

        repository.save(timeline)

        assertThat(assertTable).row(0).isEqualTo(Timelines.id, timeline.id.value)
            .isEqualTo(Timelines.userDetailId, timeline.userDetailId.id).isEqualTo(Timelines.name, timeline.name.value)
            .isEqualTo(Timelines.visibility, timeline.visibility.name).isEqualTo(Timelines.isSystem, timeline.isSystem)
    }

    @Test
    fun delete_削除される() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
            insertInto(UserDetails.tableName) {
                columns(UserDetails.columns)
                values(1, 1, "veeeeeeeeeeeeeeryStrongPassword", true, null, null)
            }
            insertInto(Timelines.tableName) {
                columns(Timelines.columns)
                values(1, 1, "test-timeline", "PUBLIC", true)
            }

        }.launch()
        val timeline = Timeline(
            id = TimelineId(1),
            userDetailId = UserDetailId(1),
            name = TimelineName("timeline"),
            visibility = TimelineVisibility.PRIVATE,
            isSystem = false
        )

        change.withSuspend {
            repository.delete(timeline)
        }

        assertThat(change).changeOfDeletionOnTable(Timelines.tableName).rowAtStartPoint().value(Timelines.id.name)
            .isEqualTo(timeline.id.value)
    }

    @Test
    fun findByIds_指定されたIdすべて返す() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
            insertInto(Timelines.tableName) {
                columns(Timelines.columns)
                values(1, 1, "test-timeline", "PUBLIC", true)
                values(2, 1, "test-timeline2", "PUBLIC", true)
                values(3, 1, "test-timeline3", "PUBLIC", true)
            }
        }.launch()

        val findByIds = repository.findByIds(listOf(TimelineId(1), TimelineId(3)))

        assertThat(findByIds).hasSize(2)
    }

    @Test
    fun findById_指定されたIdが存在したら返す() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
            insertInto(Timelines.tableName) {
                columns(Timelines.columns)
                values(1, 1, "test-timeline", "PUBLIC", true)
                values(2, 1, "test-timeline2", "PUBLIC", true)
                values(3, 1, "test-timeline3", "PUBLIC", true)
            }
        }.launch()

        val actual = repository.findById(TimelineId(1))

        val expected = Timeline(
            TimelineId(1), UserDetailId(1), TimelineName("test-timeline"), TimelineVisibility.PUBLIC, true
        )

        assertEquals(expected, actual)
        assertNotNull(actual)
        assertEquals(expected.id, actual.id)
        assertEquals(expected.userDetailId, actual.userDetailId)
        assertEquals(expected.name, actual.name)
        assertEquals(expected.visibility, actual.visibility)
        assertEquals(expected.isSystem, actual.isSystem)
    }

    @Test
    fun findById_指定されたIdがなければnull() = runTest {
        assertNull(repository.findById(TimelineId(1)))
    }

    @Test
    fun findAllByUserDetailIdANdVisibilityIn_指定されたVisibilityで指定されたUserDetailId全部返す() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
            insertInto(Timelines.tableName) {
                columns(Timelines.columns)
                values(1, 1, "test-timeline", "PUBLIC", true)
                values(2, 1, "test-timeline2", "PRIVATE", true)
                values(3, 1, "test-timeline3", "PUBLIC", true)
            }
        }.launch()

        val timelines =
            repository.findAllByUserDetailIdAndVisibilityIn(UserDetailId(1), listOf(TimelineVisibility.PUBLIC))

        assertThat(timelines).hasSize(2)
    }

    @Test
    fun save_ドメインイベントがパブリッシュされる() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
        }.launch()
        val timeline = Timeline(
            id = TimelineId(1),
            userDetailId = UserDetailId(1),
            name = TimelineName("timeline"),
            visibility = TimelineVisibility.PRIVATE,
            isSystem = false
        )

        timeline.setVisibility(
            TimelineVisibility.PUBLIC, UserDetail.create(
                UserDetailId(1), ActorId(1),
                UserDetailHashedPassword("aaaaaa"),
            )
        )

        repository.save(timeline)

        TransactionManager.current().commit()

        verify(domainEventPublisher, times(1)).publishEvent(any())
    }

    @Test
    fun delete_ドメインイベントがパブリッシュされる() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
            insertInto(UserDetails.tableName) {
                columns(UserDetails.columns)
                values(1, 1, "veeeeeeeeeeeeeeryStrongPassword", true, null, null)
            }
            insertInto(Timelines.tableName) {
                columns(Timelines.columns)
                values(1, 1, "test-timeline", "PUBLIC", true)
            }

        }.launch()
        val timeline = Timeline(
            id = TimelineId(1),
            userDetailId = UserDetailId(1),
            name = TimelineName("timeline"),
            visibility = TimelineVisibility.PRIVATE,
            isSystem = false
        )

        timeline.setVisibility(
            TimelineVisibility.PUBLIC, UserDetail.create(
                UserDetailId(1), ActorId(1),
                UserDetailHashedPassword("aaaaaa"),
            )
        )

        repository.delete(timeline)

        TransactionManager.current().commit()

        verify(domainEventPublisher, times(1)).publishEvent(any())
    }
}
