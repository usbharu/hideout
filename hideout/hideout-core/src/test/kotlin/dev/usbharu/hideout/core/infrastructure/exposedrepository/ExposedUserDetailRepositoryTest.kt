package dev.usbharu.hideout.core.infrastructure.exposedrepository

import com.ninja_squad.dbsetup_kotlin.dbSetup
import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.timeline.TimelineId
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
class ExposedUserDetailRepositoryTest : AbstractRepositoryTest(UserDetails) {

    @InjectMocks
    lateinit var userDetailRepository: ExposedUserDetailRepository

    @Mock
    lateinit var domainEventPublisher: DomainEventPublisher

    @Test
    fun save_idが同じレコードがない場合insert() = runTest {
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

        userDetailRepository.save(
            UserDetail.create(
                UserDetailId(1),
                ActorId(1),
                UserDetailHashedPassword("VeeeeeeeeeeeeeryStrongPassword"),
                false,
                null,
                null
            )
        )

        assertThat(assertTable).row(0).isEqualTo(UserDetails.id, 1).isEqualTo(UserDetails.actorId, 1)
            .isEqualTo(UserDetails.password, "VeeeeeeeeeeeeeryStrongPassword")
            .isEqualTo(UserDetails.lastMigration, null).isEqualTo(UserDetails.autoAcceptFolloweeFollowRequest, false)
            .isEqualTo(UserDetails.homeTimelineId, null)
    }

    @Test
    fun save_idが同じレコードがある場合update() = runTest {
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
            insertInto("public.user_details") {
                columns(UserDetails.columns)
                values(
                    1,
                    1,
                    "$2a$10\$EBj3lstVOv0wz3CxLpzYJu8FFrUJ2MPJW9Vlklyg.bfGEOn5sqIwm",
                    false,
                    null,
                    1832779979297918976
                )
            }
            execute(enableReferenceIntegrityConstraints)
        }.launch()

        userDetailRepository.save(
            UserDetail.create(
                UserDetailId(1),
                ActorId(1),
                UserDetailHashedPassword("VeeeeeeeeeeeeeryStrongPassword"),
                false,
                null,
                null
            )
        )

        assertThat(assertTable).row(0).isEqualTo(UserDetails.id, 1).isEqualTo(UserDetails.actorId, 1)
            .isEqualTo(UserDetails.password, "VeeeeeeeeeeeeeryStrongPassword")
            .isEqualTo(UserDetails.lastMigration, null).isEqualTo(UserDetails.autoAcceptFolloweeFollowRequest, false)
            .isEqualTo(UserDetails.homeTimelineId, null)
    }

    @Test
    fun delete_削除される() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
            insertInto("public.user_details") {
                columns(UserDetails.columns)
                values(
                    1,
                    1,
                    "$2a$10\$EBj3lstVOv0wz3CxLpzYJu8FFrUJ2MPJW9Vlklyg.bfGEOn5sqIwm",
                    false,
                    null,
                    1832779979297918976
                )
            }
            execute(enableReferenceIntegrityConstraints)
        }.launch()

        val userDetail = UserDetail(
            UserDetailId(1), ActorId(1), UserDetailHashedPassword("VeeeeeeeeeeeeeryStrongPassword"), false, null, null
        )

        change.withSuspend {
            userDetailRepository.delete(userDetail)
        }

        assertThat(change).changeOfDeletionOnTable(UserDetails.tableName).rowAtStartPoint().value(UserDetails.id.name)
            .isEqualTo(1)
    }

    @Test
    fun findByActorId_指定したActorIdで存在したら返す() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
            insertInto("public.user_details") {
                columns(UserDetails.columns)
                values(
                    1,
                    1,
                    "$2a$10\$EBj3lstVOv0wz3CxLpzYJu8FFrUJ2MPJW9Vlklyg.bfGEOn5sqIwm",
                    false,
                    null,
                    1832779979297918976
                )
            }
            execute(enableReferenceIntegrityConstraints)
        }.launch()

        val expect = UserDetail(
            id = UserDetailId(1),
            actorId = ActorId(1),
            password = UserDetailHashedPassword("$2a$10\$EBj3lstVOv0wz3CxLpzYJu8FFrUJ2MPJW9Vlklyg.bfGEOn5sqIwm"),
            autoAcceptFolloweeFollowRequest = false,
            lastMigration = null,
            homeTimelineId = TimelineId(1832779979297918976)
        )

        val actual = userDetailRepository.findByActorId(1)

        assertEquals(actual, expect)
    }

    @Test
    fun findByActorId_指定したActorIdで存在しないとnull() = runTest {
        assertNull(userDetailRepository.findByActorId(1))
    }

    @Test
    fun findById_指定したIdで存在したら返す() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
            insertInto("public.user_details") {
                columns(UserDetails.columns)
                values(
                    1,
                    1,
                    "$2a$10\$EBj3lstVOv0wz3CxLpzYJu8FFrUJ2MPJW9Vlklyg.bfGEOn5sqIwm",
                    false,
                    null,
                    1832779979297918976
                )
            }
            execute(enableReferenceIntegrityConstraints)
        }.launch()

        val expect = UserDetail(
            id = UserDetailId(1),
            actorId = ActorId(1),
            password = UserDetailHashedPassword("$2a$10\$EBj3lstVOv0wz3CxLpzYJu8FFrUJ2MPJW9Vlklyg.bfGEOn5sqIwm"),
            autoAcceptFolloweeFollowRequest = false,
            lastMigration = null,
            homeTimelineId = TimelineId(1832779979297918976)
        )

        val actual = userDetailRepository.findById(UserDetailId(1))

        assertEquals(actual, expect)
    }

    @Test
    fun findById_指定したIdで存在しないとnull() = runTest {
        assertNull(userDetailRepository.findById(UserDetailId(1)))
    }

    @Test
    fun findAllById_指定されたidすべて返す() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
            insertInto("public.user_details") {
                columns(UserDetails.columns)
                values(
                    1, 1, "$2a$10\$EBj3lstVOv0wz3CxLpzYJu8FFrUJ2MPJW9Vlklyg.bfGEOn5sqIwm", false, null, null
                )
                values(
                    2,
                    2,
                    "$2a$10\$EBj3lstVOv0wz3CxLpzYJu8FFrUJ2MPJW9Vlklyg.bfGEOn5sqIwm",
                    false,
                    null,
                    1832779979297918976
                )
                values(
                    3,
                    3,
                    "$2a$10\$EBj3lstVOv0wz3CxLpzYJu8FFrUJ2MPJW9Vlklyg.bfGEOn5sqIwm",
                    false,
                    null,
                    1832779979297918976
                )
            }
            execute(enableReferenceIntegrityConstraints)
        }.launch()

        val userDetailList = userDetailRepository.findAllById(listOf(UserDetailId(1), UserDetailId(3)))

        assertThat(userDetailList).hasSize(2)
    }

    @Test
    fun save_ドメインイベントがパブリッシュされる() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
        }.launch()

        userDetailRepository.save(
            UserDetail.create(
                UserDetailId(1),
                ActorId(1),
                UserDetailHashedPassword("VeeeeeeeeeeeeeryStrongPassword"),
                false,
                null,
                null
            )
        )

        TransactionManager.current().commit()

        verify(domainEventPublisher, times(1)).publishEvent(any())
    }

    @Test
    fun delete_ドメインイベントがパブリッシュされる() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
            insertInto("public.user_details") {
                columns(
                    UserDetails.columns
                )
                values(
                    1,
                    1,
                    "$2a$10\$EBj3lstVOv0wz3CxLpzYJu8FFrUJ2MPJW9Vlklyg.bfGEOn5sqIwm",
                    false,
                    null,
                    1832779979297918976
                )
            }
            execute(enableReferenceIntegrityConstraints)
        }.launch()

        userDetailRepository.delete(
            UserDetail.create(
                UserDetailId(1),
                ActorId(1),
                UserDetailHashedPassword("VeeeeeeeeeeeeeryStrongPassword"),
                false,
                null,
                null
            )
        )

        TransactionManager.current().commit()

        verify(domainEventPublisher, times(1)).publishEvent(any())
    }

    private fun assertEquals(
        actual: UserDetail?, expect: UserDetail
    ) {
        assertNotNull(actual)
        kotlin.test.assertEquals(expect, actual)
        assertEquals(expect.id, actual.id)
        assertEquals(expect.actorId, actual.actorId)
        assertEquals(expect.password, actual.password)
        assertEquals(expect.autoAcceptFolloweeFollowRequest, actual.autoAcceptFolloweeFollowRequest)
        assertEquals(expect.lastMigration, actual.lastMigration)
        assertEquals(expect.homeTimelineId, actual.homeTimelineId)
    }
}
