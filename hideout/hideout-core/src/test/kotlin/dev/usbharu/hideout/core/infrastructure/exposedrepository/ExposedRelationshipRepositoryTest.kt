package dev.usbharu.hideout.core.infrastructure.exposedrepository

import com.ninja_squad.dbsetup_kotlin.dbSetup
import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.relationship.Relationship
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEventPublisher
import kotlinx.coroutines.test.runTest
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
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@ExtendWith(MockitoExtension::class)
class ExposedRelationshipRepositoryTest : AbstractRepositoryTest(Relationships) {

    @InjectMocks
    lateinit var repository: ExposedRelationshipRepository

    @Mock
    lateinit var domainEventPublisher: DomainEventPublisher

    @Test
    fun save_idが同じレコードがなければinsert() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
            insertInto(Actors.tableName) {
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

        val relationship = Relationship.default(
            ActorId(1), ActorId(2)
        )

        repository.save(relationship)

        assertThat(assertTable)
            .row(0)
            .isEqualTo(Relationships.actorId, relationship.actorId.id)
            .isEqualTo(Relationships.targetActorId, relationship.targetActorId.id)
            .isEqualTo(Relationships.following, relationship.following)
            .isEqualTo(Relationships.blocking, relationship.blocking)
            .isEqualTo(Relationships.muting, relationship.muting)
            .isEqualTo(Relationships.followRequesting, relationship.followRequesting)
            .isEqualTo(Relationships.mutingFollowRequest, relationship.mutingFollowRequest)
    }

    @Test
    fun save_idが同じレコードがあればupdate() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
            insertInto(Actors.tableName) {
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
            insertInto(Relationships.tableName) {
                columns(Relationships.columns)
                values(1, 2, true, false, false, false, false)
            }
        }.launch()

        val relationship = Relationship(
            actorId = ActorId(1),
            targetActorId = ActorId(2),
            following = false,
            blocking = true,
            muting = false,
            followRequesting = false,
            mutingFollowRequest = false,
        )

        repository.save(relationship)

        assertThat(assertTable)
            .row(0)
            .isEqualTo(Relationships.actorId, relationship.actorId.id)
            .isEqualTo(Relationships.targetActorId, relationship.targetActorId.id)
            .isEqualTo(Relationships.following, relationship.following)
            .isEqualTo(Relationships.blocking, relationship.blocking)
            .isEqualTo(Relationships.muting, relationship.muting)
            .isEqualTo(Relationships.followRequesting, relationship.followRequesting)
            .isEqualTo(Relationships.mutingFollowRequest, relationship.mutingFollowRequest)
    }

    @Test
    fun delete_削除される() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
            insertInto(Actors.tableName) {
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
            insertInto(Relationships.tableName) {
                columns(Relationships.columns)
                values(1, 2, true, false, false, false, false)
            }
        }.launch()

        val relationship = Relationship.default(
            ActorId(1), ActorId(2)
        )

        change.withSuspend {
            repository.delete(relationship)
        }

        assertThat(change)
            .changeOfDeletionOnTable(Relationships.tableName)
            .rowAtStartPoint()
            .value(Relationships.actorId.name).isEqualTo(relationship.actorId.id)
            .value(Relationships.targetActorId.name).isEqualTo(relationship.targetActorId.id)

    }

    @Test
    fun findByActorIdAndTargetId_指定された条件で存在したら返す() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
            insertInto(Relationships.tableName) {
                columns(Relationships.columns)
                values(1, 2, true, false, true, true, false)
                values(2, 1, true, false, false, false, false)
                values(4, 3, true, false, false, false, true)
                values(3, 5, true, false, false, true, false)
            }
        }.launch()

        val expected = Relationship(
            actorId = ActorId(1),
            targetActorId = ActorId(2),
            following = true,
            blocking = false,
            muting = true,
            followRequesting = true,
            mutingFollowRequest = false
        )

        val actual = repository.findByActorIdAndTargetId(ActorId(1), ActorId(2))

        assertEquals(expected, actual)
        assertNotNull(actual)
        assertEquals(expected.actorId, actual.actorId)
        assertEquals(expected.targetActorId, actual.targetActorId)
        assertEquals(expected.following, actual.following)
        assertEquals(expected.blocking, actual.blocking)
        assertEquals(expected.muting, actual.muting)
        assertEquals(expected.followRequesting, actual.followRequesting)
        assertEquals(expected.mutingFollowRequest, actual.mutingFollowRequest)
    }

    @Test
    fun findByActorIdAndTargetId_指定された条件で存在しないとnull() = runTest {
        assertNull(repository.findByActorIdAndTargetId(ActorId(1), ActorId(2)))
    }

    @Test
    fun findByActorIdsAndTargetIdAndBlocking_指定された条件全部返す() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
            insertInto(Relationships.tableName) {
                columns(Relationships.columns)
                values(1, 2, true, false, true, true, false)
                values(1, 3, false, true, true, true, false)
                values(1, 4, true, false, true, true, false)
                values(2, 1, true, false, false, false, false)
                values(4, 3, true, false, false, false, true)
                values(3, 5, true, false, false, true, false)
            }
        }.launch()

        val expected = listOf(
            Relationship(
                actorId = ActorId(1),
                targetActorId = ActorId(3),
                following = false,
                blocking = true,
                muting = true,
                followRequesting = true,
                mutingFollowRequest = false
            )
        )

        val actual = repository.findByActorIdsAndTargetIdAndBlocking(
            listOf(ActorId(1), ActorId(4)), ActorId(3), blocking = true
        )

        assertContentEquals(expected, actual)

        val expected2 = listOf(
            Relationship(
                actorId = ActorId(4),
                targetActorId = ActorId(3),
                following = true,
                blocking = false,
                muting = false,
                followRequesting = false,
                mutingFollowRequest = true
            )
        )

        val actual2 = repository.findByActorIdsAndTargetIdAndBlocking(
            listOf(ActorId(1), ActorId(4)), ActorId(3), blocking = false
        )

        assertContentEquals(expected2, actual2)
    }

    @Test
    fun findByActorIdAndTargetIdsAndFollowing_指定された条件全部返す() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
            insertInto(Relationships.tableName) {
                columns(Relationships.columns)
                values(1, 2, true, false, true, true, false)
                values(1, 3, false, false, true, true, false)
                values(1, 4, true, false, true, true, false)
                values(2, 1, true, false, false, false, false)
                values(4, 3, true, false, false, false, true)
                values(3, 5, true, false, false, true, false)
            }
        }.launch()

        val expected = listOf(
            Relationship(
                actorId = ActorId(1),
                targetActorId = ActorId(2),
                following = false,
                blocking = true,
                muting = true,
                followRequesting = true,
                mutingFollowRequest = false
            ), Relationship(
                actorId = ActorId(1),
                targetActorId = ActorId(4),
                following = false,
                blocking = true,
                muting = true,
                followRequesting = true,
                mutingFollowRequest = false
            )
        )

        val actual = repository.findByActorIdAndTargetIdsAndFollowing(
            ActorId(1), listOf(ActorId(2), ActorId(3), ActorId(4)), following = true
        )

        assertContentEquals(expected, actual)

        val expected2 = listOf(
            Relationship(
                actorId = ActorId(1),
                targetActorId = ActorId(3),
                following = true,
                blocking = false,
                muting = false,
                followRequesting = false,
                mutingFollowRequest = true
            )
        )

        val actual2 = repository.findByActorIdAndTargetIdsAndFollowing(
            ActorId(1), listOf(ActorId(2), ActorId(3), ActorId(4)), following = false
        )

        assertContentEquals(expected2, actual2)
    }

    @Test
    fun save_ドメインイベントがパブリッシュされる() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
        }.launch()

        val relationship = Relationship(
            actorId = ActorId(1),
            targetActorId = ActorId(2),
            following = false,
            blocking = true,
            muting = false,
            followRequesting = false,
            mutingFollowRequest = false,
        )

        relationship.mute()

        repository.save(relationship)

        TransactionManager.current().commit()

        verify(domainEventPublisher, times(1)).publishEvent(any())
    }

    @Test
    fun delete_ドメインイベントがパブリッシュされる() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
            insertInto(Relationships.tableName) {
                columns(Relationships.columns)
                values(1, 2, true, false, false, false, false)
            }
        }.launch()

        val relationship = Relationship(
            actorId = ActorId(1),
            targetActorId = ActorId(2),
            following = false,
            blocking = true,
            muting = false,
            followRequesting = false,
            mutingFollowRequest = false,
        )
        relationship.mute()

        repository.delete(relationship)

        TransactionManager.current().commit()

        verify(domainEventPublisher, times(1)).publishEvent(any())
    }
}