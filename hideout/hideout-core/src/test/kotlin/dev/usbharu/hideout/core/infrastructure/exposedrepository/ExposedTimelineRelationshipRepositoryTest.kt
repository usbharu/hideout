package dev.usbharu.hideout.core.infrastructure.exposedrepository

import com.ninja_squad.dbsetup_kotlin.dbSetup
import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.timeline.TimelineId
import dev.usbharu.hideout.core.domain.model.timelinerelationship.TimelineRelationship
import dev.usbharu.hideout.core.domain.model.timelinerelationship.TimelineRelationshipId
import dev.usbharu.hideout.core.domain.model.timelinerelationship.Visible
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.db.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import utils.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ExposedTimelineRelationshipRepositoryTest : AbstractRepositoryTest(TimelineRelationships) {

    @Test
    fun save_idが同じレコードがなければinsert() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
            insertInto(Timelines.tableName) {
                columns(Timelines.columns)
                values(1, 1, "time-line", "PUBLIC", false)
            }
            insertInto("public.actors") {
                columns(
                    Actors.columns
                )
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

        val relationship = TimelineRelationship(
            TimelineRelationshipId(1), TimelineId(1), ActorId(1), Visible.PUBLIC
        )
        ExposedTimelineRelationshipRepository().save(relationship)

        assertThat(assertTable)
            .row(0)
            .isEqualTo(TimelineRelationships.id, relationship.id.value)
            .isEqualTo(TimelineRelationships.timelineId, relationship.timelineId.value)
            .isEqualTo(TimelineRelationships.actorId, relationship.actorId.id)
            .isEqualTo(TimelineRelationships.visible, relationship.visible.name)
    }

    @Test
    fun save_idが同じレコードがあればupdate() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
            insertInto(Timelines.tableName) {
                columns(Timelines.columns)
                values(1, 1, "time-line", "PUBLIC", false)
            }
            insertInto(TimelineRelationships.tableName) {
                columns(TimelineRelationships.columns)
                values(1, 2, 3, "PUBLIC")
            }
            insertInto("public.actors") {
                columns(
                    Actors.columns
                )
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

        val relationship = TimelineRelationship(
            TimelineRelationshipId(1), TimelineId(1), ActorId(1), Visible.PUBLIC
        )
        ExposedTimelineRelationshipRepository().save(relationship)

        assertThat(assertTable)
            .row(0)
            .isEqualTo(TimelineRelationships.id, relationship.id.value)
            .isEqualTo(TimelineRelationships.timelineId, relationship.timelineId.value)
            .isEqualTo(TimelineRelationships.actorId, relationship.actorId.id)
            .isEqualTo(TimelineRelationships.visible, relationship.visible.name)
    }

    @Test
    fun delete_削除される() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
            insertInto(TimelineRelationships.tableName) {
                columns(TimelineRelationships.columns)
                values(1, 2, 3, "PUBLIC")
            }
            execute(enableReferenceIntegrityConstraints)
        }.launch()

        val timelineRelationship = TimelineRelationship(
            TimelineRelationshipId(1),
            TimelineId(1), ActorId
                (3), Visible.PUBLIC
        )

        change.withSuspend {
            ExposedTimelineRelationshipRepository().delete(timelineRelationship)
        }

        assertThat(change)
            .changeOfDeletionOnTable(TimelineRelationships.tableName)
            .rowAtStartPoint()
            .value(TimelineRelationships.id.name).isEqualTo(timelineRelationship.id.value)
    }

    @Test
    fun findByActorId_指定されたid全部返す() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
            insertInto(TimelineRelationships.tableName) {
                columns(TimelineRelationships.columns)
                values(1, 2, 3, "PUBLIC")
                values(2, 3, 3, "PUBLIC")
                values(3, 3, 4, "PUBLIC")
            }
            execute(enableReferenceIntegrityConstraints)
        }.launch()


        val findByActorId = ExposedTimelineRelationshipRepository().findByActorId(actorId = ActorId(3))

        assertThat(findByActorId)
            .hasSize(2)
    }

    @Test
    fun findById_指定されたidで存在したら返す() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
            insertInto(TimelineRelationships.tableName) {
                columns(TimelineRelationships.columns)
                values(1, 2, 3, "PUBLIC")
            }
            execute(enableReferenceIntegrityConstraints)
        }.launch()

        val expected = TimelineRelationship(
            id = TimelineRelationshipId(1),
            timelineId = TimelineId(2),
            actorId = ActorId
                (3),
            visible = Visible.PUBLIC
        )

        val actual = ExposedTimelineRelationshipRepository().findById(TimelineRelationshipId(1))

        assertEquals(expected, actual)
        assertNotNull(actual)
        assertEquals(expected.id, actual.id)
        assertEquals(expected.actorId, actual.actorId)
        assertEquals(expected.timelineId, actual.timelineId)
        assertEquals(expected.visible, actual.visible)
    }

    @Test
    fun findById_指定されたIdで存在しないとnull() = runTest {
        assertNull(ExposedTimelineRelationshipRepository().findById(TimelineRelationshipId(1)))
    }

    @Test
    fun findByTimelineIdAndActorId_指定された条件で存在したら返す() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
            insertInto(TimelineRelationships.tableName) {
                columns(TimelineRelationships.columns)
                values(1, 2, 3, "PUBLIC")
            }
            execute(enableReferenceIntegrityConstraints)
        }.launch()

        val expected = TimelineRelationship(
            id = TimelineRelationshipId(1),
            timelineId = TimelineId(2),
            actorId = ActorId
                (3),
            visible = Visible.PUBLIC
        )

        val actual = ExposedTimelineRelationshipRepository().findByTimelineIdAndActorId(TimelineId(2), ActorId(3))

        assertEquals(expected, actual)
        assertNotNull(actual)
        assertEquals(expected.id, actual.id)
        assertEquals(expected.actorId, actual.actorId)
        assertEquals(expected.timelineId, actual.timelineId)
        assertEquals(expected.visible, actual.visible)
    }

    @Test
    fun findByTimelineIdAndActorId_指定された条件で存在しないとnull() = runTest {
        ExposedTimelineRelationshipRepository().findByTimelineIdAndActorId(TimelineId(1), ActorId(1))
    }
}