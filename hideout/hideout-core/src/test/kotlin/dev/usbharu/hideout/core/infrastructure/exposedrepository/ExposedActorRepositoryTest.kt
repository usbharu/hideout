package dev.usbharu.hideout.core.infrastructure.exposedrepository

import com.ninja_squad.dbsetup_kotlin.dbSetup
import dev.usbharu.hideout.core.domain.model.actor.*
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEventPublisher
import dev.usbharu.hideout.core.infrastructure.exposed.ActorQueryMapper
import dev.usbharu.hideout.core.infrastructure.exposed.ActorResultRowMapper
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
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@ExtendWith(MockitoExtension::class)
class ExposedActorRepositoryTest : AbstractRepositoryTest(Actors) {

    @InjectMocks
    lateinit var repository: ExposedActorRepository

    @Mock
    lateinit var domainEventPublisher: DomainEventPublisher

    @Spy
    val actorQueryMapper = ActorQueryMapper(ActorResultRowMapper())

    @Test
    fun save_idが同じレコードがない場合はinsert() = runTest {
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

        val actor = TestActorFactory.create()

        repository.save(actor)

        assertThat(assertTable)
            .row(1)
            .isEqualTo(Actors.id, actor.id.id)
            .isEqualTo(Actors.name, actor.name.name)
            .isEqualTo(Actors.domain, actor.domain.domain)
            .isEqualTo(Actors.screenName, actor.screenName.screenName)
            .isEqualTo(Actors.description, actor.description.description)
            .value(Actors.url).isEqualTo(actor.url.toString())
            .value(Actors.inbox).isEqualTo(actor.inbox.toString())
            .value(Actors.outbox).isEqualTo(actor.outbox.toString())
            .isEqualTo(Actors.publicKey, actor.publicKey.publicKey)
            .isEqualTo(Actors.privateKey, actor.privateKey?.privateKey)
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
            insertInto(ActorsAlsoKnownAs.tableName) {
                columns(ActorsAlsoKnownAs.columns)
                values(1, 2)
            }
            execute(enableReferenceIntegrityConstraints)
        }.launch()

        val actor = TestActorFactory.create(1, alsoKnownAs = setOf(ActorId(1)))

        repository.save(actor)

        assertThat(assertTable)
            .row(1)
            .isEqualTo(Actors.id, actor.id.id)
            .isEqualTo(Actors.name, actor.name.name)
            .isEqualTo(Actors.domain, actor.domain.domain)
            .isEqualTo(Actors.screenName, actor.screenName.screenName)
            .isEqualTo(Actors.description, actor.description.description)
            .value(Actors.url).isEqualTo(actor.url.toString())
            .value(Actors.inbox).isEqualTo(actor.inbox.toString())
            .value(Actors.outbox).isEqualTo(actor.outbox.toString())
            .isEqualTo(Actors.publicKey, actor.publicKey.publicKey)
            .isEqualTo(Actors.privateKey, actor.privateKey?.privateKey)

        assertThat(getTable(ActorsAlsoKnownAs.tableName))
            .row(0)
            .isEqualTo(ActorsAlsoKnownAs.actorId, 1)
            .isEqualTo(ActorsAlsoKnownAs.alsoKnownAs, 1)
    }

    @Test
    fun delete_削除される() = runTest {
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
            insertInto(ActorsAlsoKnownAs.tableName) {
                columns(ActorsAlsoKnownAs.columns)
                values(1, 1)
            }
        }.launch()

        val actor = TestActorFactory.create(1, alsoKnownAs = setOf(ActorId(1)))

        val changes = Changes(dataSource)
        changes.withSuspend {
            repository.delete(actor)
        }

        assertThat(changes)
            .changeOfDeletionOnTable(Actors.tableName)
            .rowAtStartPoint()
            .value(Actors.id.name).isEqualTo(actor.id.id)
            .changeOfDeletionOnTable(ActorsAlsoKnownAs.tableName)
            .rowAtStartPoint()
            .value(ActorsAlsoKnownAs.alsoKnownAs.name)
            .isEqualTo(actor.alsoKnownAs.first().id)
    }

    @Test
    fun findById_指定されたIdがあれば返す() = runTest {
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
        }.launch()

        val expect = TestActorFactory.create(
            id = 1,
            actorName = "b",
            domain = "test-hideout-dev.usbharu.dev",
            actorScreenName = "b",
            description = "",
            inbox = URI.create("https://test-hideout-dev.usbharu.dev/users/b/inbox"),
            outbox = URI.create("https://test-hideout-dev.usbharu.dev/users/b/outbox"),
            uri = URI.create("https://test-hideout-dev.usbharu.dev/users/b"),
            publicKey = ActorPublicKey("-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyuMjzmQBsSxzK6NkOpZh\nWuohaUbzCY7AafXt+3+tiL6LulYNg/YRIqKc7Q/vTJE6CHrqo7RA/OqYrSMxF/LC\nf8aX5aHwJE1A2gSgCcs1IL5GJaYRlp4NcuazpBC9NO4xIrvH//jcVnZGXGWsCbls\nHXZGZdurWOF0Bl3mYN8CdupVumrGuOPs+wbI/Gh+OHw611TcXMyAwFwU2UjvPEgk\nEACW9OvJaq1K40jVCAa3b1nXt53vlXXZEUlL78L0C9xuFbJG0K/GKMBN44GyftJO\nhA95Rf1Nhd0vKDLBiRocGcARmBo9PaSCR5651gJEk5/wfLUnNAf0xj3R8LBoOhnT\nCQIDAQAB\n-----END PUBLIC KEY-----"),
            privateKey = ActorPrivateKey(
                "-----BEGIN PRIVATE KEY-----\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDK4yPOZAGxLHMr\no2Q6lmFa6iFpRvMJjsBp9e37f62Ivou6Vg2D9hEiopztD+9MkToIeuqjtED86pit\nIzEX8sJ/xpflofAkTUDaBKAJyzUgvkYlphGWng1y5rOkEL007jEiu8f/+NxWdkZc\nZawJuWwddkZl26tY4XQGXeZg3wJ26lW6asa44+z7Bsj8aH44fDrXVNxczIDAXBTZ\nSO88SCQQAJb068lqrUrjSNUIBrdvWde3ne+VddkRSUvvwvQL3G4VskbQr8YowE3j\ngbJ+0k6ED3lF/U2F3S8oMsGJGhwZwBGYGj09pIJHnrnWAkSTn/B8tSc0B/TGPdHw\nsGg6GdMJAgMBAAECggEAHkEhLEb70kdOGgJLUR9D/5zYBE0eXdz/MsMyd1AH+Shs\n9AmetKsYzWDmuhp9Cp5swyn328Hmn7B+DvInVn+5YvjNhY07SbaJcVls4g5UQFXk\nu6WC4ZfKap7IyAeaUg54858r8677xcWXuByN5dn+1iU2hJGYK3Cx7rx0PRrUURYG\n2BRaEEwkcPNm9u679OOTyvTmA3NhewUuDaTMkZnnAml87uYYnmFKjQcR+S2UqOm6\nvBZ/devG4TfPBeKEAya/ba8JJ8frGOtjmR9EIliTQoxI2izeAfoGs1OsCSpuPy6s\nV5f0X3HYM7CA+Fpkt2pnixuwg96LaVr4OpVxujhNlwKBgQD1827VuKFGrneNO+c+\n4EIvh+vLh462bJiaVsMHfRhNZF1/5i8gfNJ16ST60hJo11E4riHPzi3q6GWuxOYl\nCkVKvhJ2g3mgnhoehcgnT7UBkasaC7JYd+LsFDnWOTVSJOy2OqfLdLDGAuSTN3kO\nBF4p0ZqQ/AouFNin57WNRGVZ7wKBgQDTLUZtfTkOU3G1nIMTRKmZjqdER5glzHCm\n9o/1ZsQktL+nzSXqYeoWh9fr7fkmC0k/07+SHzzfWvOhWWWlRenUVL5mj7FRq+L9\n9kDjChLR3Jr4L6Sj1iaQ+0uqDSQNYSYO9ctMjAVjFiNhiAd+S6B451Q1VbDKTCHt\nkRW9omz6hwKBgBFTsgY6eJorJl77zmG+mMsSb0kqZqJxahrNa/X2GSUyoeelxsIq\nKQWHhERrUkKykJVGpzkllFSNRMSYOIJ5g8ItO82/m2z2Vm66DAzA78aJhZ1TH6Bd\n6c2p6x0tcJU15rs7zKBnuyBoCcRZTxzur9eQXaxDJVBzxYOmrkKig+VfAoGBAMCP\n2Fiehxh5HobsYNmBEuXjHsM0RZiyA0c8LakoPFL8PodUme5PupUw6cNJDJeUUwbQ\nny8vLOK+nMnUKsu6JK5pV/VNsfM3OZU6p5Bf7ylOcEE/sHF1JVWu0CAQO3+3xmx9\n1RPH2mGwHjMhRzPy4jFdP3wi10KgiY+HbLuvEJChAoGAYCsh3UhtTzGUOlPBkmLL\n17bD0wN4J/fOv8BoXPZ8H2CdqVgWy0s+s+QaPqRxNcA6YyGymBqrmQAn1Uii25r9\nKAwVAjg3S2KDEMSI2RbMMmQJSZ1u0GkxqOUC/MMeZqBYTYxVeqcQPoqJZ0Nk7IOA\nZPFif8bVfcZqeimxrFaV6YI=\n-----END PRIVATE KEY-----",
            ),
            createdAt = Timestamp.valueOf("2024-09-09 17:12:03.941339").toInstant(),
            keyId = "https://test-hideout-dev.usbharu.dev/users/b#main-key",
            followingEndpoint = URI.create("https://test-hideout-dev.usbharu.dev/users/b/following"),
            followersEndpoint = URI.create("https://test-hideout-dev.usbharu.dev/users/b/followers"),
            instanceId = 1,
            locked = false,
            followersCount = 0,
            followingCount = 0,
            postCount = 0,
            lastPostDate = null,
            lastUpdateAt = Timestamp.valueOf("2024-09-09 17:12:03.941339").toInstant(),
            suspend = false,
            alsoKnownAs = emptySet(),
            moveTo = null,
            emojiIds = emptySet(),
            deleted = false,
            banner = null,
            icon = null
        )

        val actual = repository.findById(ActorId(1))

        assertEquals(actual, expect)
    }

    private fun assertEquals(
        actual: Actor?,
        expect: Actor
    ) {
        assertNotNull(actual)
        kotlin.test.assertEquals(expect, actual)
        assertEquals(expect.id, actual.id)
        assertEquals(expect.name, actual.name)
        assertEquals(expect.domain, actual.domain)
        assertEquals(expect.screenName, actual.screenName)
        assertEquals(expect.description, actual.description)
        assertEquals(expect.inbox, actual.inbox)
        assertEquals(expect.outbox, actual.outbox)
        assertEquals(expect.url, actual.url)
        assertEquals(expect.publicKey, actual.publicKey)
        assertEquals(expect.privateKey, actual.privateKey)
        assertEquals(expect.createdAt, actual.createdAt)
        assertEquals(expect.keyId, actual.keyId)
        assertEquals(expect.followingEndpoint, actual.followingEndpoint)
        assertEquals(expect.followersEndpoint, actual.followersEndpoint)
        assertEquals(expect.postsCount, actual.postsCount)
        assertEquals(expect.lastPostAt, actual.lastPostAt)
        assertEquals(expect.lastUpdateAt, actual.lastUpdateAt)
        assertEquals(expect.suspend, actual.suspend)
        assertEquals(expect.moveTo, actual.moveTo)
        assertEquals(expect.emojis, actual.emojis)
        assertEquals(expect.deleted, actual.deleted)
        assertEquals(expect.banner, actual.banner)
        assertEquals(expect.icon, actual.icon)
        assertEquals(expect.banner, actual.banner)
    }

    @Test
    fun findById_指定されたIdがなければnull() = runTest {
        assertNull(repository.findById(ActorId(1)))
    }

    @Test
    fun findByNameAndDomain_指定されたNameとDomainがあれば返す() = runTest {
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
        }.launch()

        val expect = TestActorFactory.create(
            id = 1,
            actorName = "b",
            domain = "test-hideout-dev.usbharu.dev",
            actorScreenName = "b",
            description = "",
            inbox = URI.create("https://test-hideout-dev.usbharu.dev/users/b/inbox"),
            outbox = URI.create("https://test-hideout-dev.usbharu.dev/users/b/outbox"),
            uri = URI.create("https://test-hideout-dev.usbharu.dev/users/b"),
            publicKey = ActorPublicKey("-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyuMjzmQBsSxzK6NkOpZh\nWuohaUbzCY7AafXt+3+tiL6LulYNg/YRIqKc7Q/vTJE6CHrqo7RA/OqYrSMxF/LC\nf8aX5aHwJE1A2gSgCcs1IL5GJaYRlp4NcuazpBC9NO4xIrvH//jcVnZGXGWsCbls\nHXZGZdurWOF0Bl3mYN8CdupVumrGuOPs+wbI/Gh+OHw611TcXMyAwFwU2UjvPEgk\nEACW9OvJaq1K40jVCAa3b1nXt53vlXXZEUlL78L0C9xuFbJG0K/GKMBN44GyftJO\nhA95Rf1Nhd0vKDLBiRocGcARmBo9PaSCR5651gJEk5/wfLUnNAf0xj3R8LBoOhnT\nCQIDAQAB\n-----END PUBLIC KEY-----"),
            privateKey = ActorPrivateKey(
                "-----BEGIN PRIVATE KEY-----\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDK4yPOZAGxLHMr\no2Q6lmFa6iFpRvMJjsBp9e37f62Ivou6Vg2D9hEiopztD+9MkToIeuqjtED86pit\nIzEX8sJ/xpflofAkTUDaBKAJyzUgvkYlphGWng1y5rOkEL007jEiu8f/+NxWdkZc\nZawJuWwddkZl26tY4XQGXeZg3wJ26lW6asa44+z7Bsj8aH44fDrXVNxczIDAXBTZ\nSO88SCQQAJb068lqrUrjSNUIBrdvWde3ne+VddkRSUvvwvQL3G4VskbQr8YowE3j\ngbJ+0k6ED3lF/U2F3S8oMsGJGhwZwBGYGj09pIJHnrnWAkSTn/B8tSc0B/TGPdHw\nsGg6GdMJAgMBAAECggEAHkEhLEb70kdOGgJLUR9D/5zYBE0eXdz/MsMyd1AH+Shs\n9AmetKsYzWDmuhp9Cp5swyn328Hmn7B+DvInVn+5YvjNhY07SbaJcVls4g5UQFXk\nu6WC4ZfKap7IyAeaUg54858r8677xcWXuByN5dn+1iU2hJGYK3Cx7rx0PRrUURYG\n2BRaEEwkcPNm9u679OOTyvTmA3NhewUuDaTMkZnnAml87uYYnmFKjQcR+S2UqOm6\nvBZ/devG4TfPBeKEAya/ba8JJ8frGOtjmR9EIliTQoxI2izeAfoGs1OsCSpuPy6s\nV5f0X3HYM7CA+Fpkt2pnixuwg96LaVr4OpVxujhNlwKBgQD1827VuKFGrneNO+c+\n4EIvh+vLh462bJiaVsMHfRhNZF1/5i8gfNJ16ST60hJo11E4riHPzi3q6GWuxOYl\nCkVKvhJ2g3mgnhoehcgnT7UBkasaC7JYd+LsFDnWOTVSJOy2OqfLdLDGAuSTN3kO\nBF4p0ZqQ/AouFNin57WNRGVZ7wKBgQDTLUZtfTkOU3G1nIMTRKmZjqdER5glzHCm\n9o/1ZsQktL+nzSXqYeoWh9fr7fkmC0k/07+SHzzfWvOhWWWlRenUVL5mj7FRq+L9\n9kDjChLR3Jr4L6Sj1iaQ+0uqDSQNYSYO9ctMjAVjFiNhiAd+S6B451Q1VbDKTCHt\nkRW9omz6hwKBgBFTsgY6eJorJl77zmG+mMsSb0kqZqJxahrNa/X2GSUyoeelxsIq\nKQWHhERrUkKykJVGpzkllFSNRMSYOIJ5g8ItO82/m2z2Vm66DAzA78aJhZ1TH6Bd\n6c2p6x0tcJU15rs7zKBnuyBoCcRZTxzur9eQXaxDJVBzxYOmrkKig+VfAoGBAMCP\n2Fiehxh5HobsYNmBEuXjHsM0RZiyA0c8LakoPFL8PodUme5PupUw6cNJDJeUUwbQ\nny8vLOK+nMnUKsu6JK5pV/VNsfM3OZU6p5Bf7ylOcEE/sHF1JVWu0CAQO3+3xmx9\n1RPH2mGwHjMhRzPy4jFdP3wi10KgiY+HbLuvEJChAoGAYCsh3UhtTzGUOlPBkmLL\n17bD0wN4J/fOv8BoXPZ8H2CdqVgWy0s+s+QaPqRxNcA6YyGymBqrmQAn1Uii25r9\nKAwVAjg3S2KDEMSI2RbMMmQJSZ1u0GkxqOUC/MMeZqBYTYxVeqcQPoqJZ0Nk7IOA\nZPFif8bVfcZqeimxrFaV6YI=\n-----END PRIVATE KEY-----",
            ),
            createdAt = Timestamp.valueOf("2024-09-09 17:12:03.941339").toInstant(),
            keyId = "https://test-hideout-dev.usbharu.dev/users/b#main-key",
            followingEndpoint = URI.create("https://test-hideout-dev.usbharu.dev/users/b/following"),
            followersEndpoint = URI.create("https://test-hideout-dev.usbharu.dev/users/b/followers"),
            instanceId = 1,
            locked = false,
            followersCount = 0,
            followingCount = 0,
            postCount = 0,
            lastPostDate = null,
            lastUpdateAt = Timestamp.valueOf("2024-09-09 17:12:03.941339").toInstant(),
            suspend = false,
            alsoKnownAs = emptySet(),
            moveTo = null,
            emojiIds = emptySet(),
            deleted = false,
            banner = null,
            icon = null
        )

        val actual = repository.findByNameAndDomain("b", "test-hideout-dev.usbharu.dev")

        assertEquals(actual, expect)
    }

    @Test
    fun findByNameAndDomain_指定されたNameとDomainがなければnull() = runTest {
        assertNull(repository.findByNameAndDomain("a", "b"))
    }

    @Test
    fun findAllById_指定されたIdすべて返す() = runTest {
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
        }.launch()

        val findAllById = repository.findAllById(listOf(ActorId(1), ActorId(2)))

        assertThat(findAllById)
            .hasSize(2)
    }

    @Test
    fun save_ドメインイベントがパブリッシュされる() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
        }.launch()

        val actor = TestActorFactory.create()
        actor.checkUpdate()
        repository.save(actor)

        TransactionManager.current().commit()

        verify(domainEventPublisher, times(1)).publishEvent(any())
    }

    @Test
    fun delete_ドメインイベントがパブリッシュされる() = runTest {
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
            insertInto(ActorsAlsoKnownAs.tableName) {
                columns(ActorsAlsoKnownAs.columns)
                values(1, 1)
            }
        }.launch()

        val actor = TestActorFactory.create(1, alsoKnownAs = setOf(ActorId(1)))
        actor.delete()

        repository.delete(actor)

        TransactionManager.current().commit()

        verify(domainEventPublisher, times(1)).publishEvent(any())
    }
}