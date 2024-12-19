package dev.usbharu.hideout.core.application.actor

import dev.usbharu.hideout.core.application.exception.InternalServerException
import dev.usbharu.hideout.core.application.exception.PermissionDeniedException
import dev.usbharu.hideout.core.domain.model.actor.Actor
import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.domain.model.actor.TestActorFactory
import dev.usbharu.hideout.core.domain.model.support.acct.Acct
import dev.usbharu.hideout.core.domain.model.support.principal.LocalUser
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetail
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailHashedPassword
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailId
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailRepository
import dev.usbharu.hideout.core.domain.service.actor.local.AccountMigrationCheck
import dev.usbharu.hideout.core.domain.service.actor.local.LocalActorMigrationCheckDomainService
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import utils.TestTransaction

@ExtendWith(MockitoExtension::class)
class MigrationLocalActorApplicationServiceTest {
    @InjectMocks
    lateinit var service: MigrationLocalActorApplicationService

    @Mock
    lateinit var actorRepository: ActorRepository

    @Mock
    lateinit var localActorMigrationCheckDomainService: LocalActorMigrationCheckDomainService

    @Mock
    lateinit var userDetailRepository: UserDetailRepository

    @Spy
    val transaction = TestTransaction

    @Test
    fun pricinpalのactorとfromのactorが違うと失敗() = runTest {
        assertThrows<PermissionDeniedException> {
            service.execute(
                MigrationLocalActor(1, 2),
                LocalUser(ActorId(3), UserDetailId(3), Acct("test", "example.com"))
            )
        }
    }

    @Test
    fun fromのactorが見つからなかったら失敗() = runTest {
        val userDetail = UserDetail.create(
            UserDetailId(1),
            ActorId(1), UserDetailHashedPassword
                ("")
        )
        whenever(userDetailRepository.findById(UserDetailId(1))).doReturn(userDetail)
        assertThrows<IllegalArgumentException> {
            service.execute(
                MigrationLocalActor(1, 2),
                LocalUser(ActorId(1), UserDetailId(1), Acct("test", "example.com"))
            )
        }
    }

    @Test
    fun toのactorが見つからなかったら失敗() = runTest {
        val userDetail = UserDetail.create(
            UserDetailId(1),
            ActorId(1), UserDetailHashedPassword
                ("")
        )
        whenever(actorRepository.findById(ActorId(1))).doReturn(TestActorFactory.create(1))
        whenever(userDetailRepository.findById(UserDetailId(1))).doReturn(userDetail)
        assertThrows<IllegalArgumentException> {
            service.execute(
                MigrationLocalActor(1, 2),
                LocalUser(ActorId(1), UserDetailId(1), Acct("test", "example.com"))
            )
        }
    }

    @Test
    fun userDetailが見つからなかったら失敗() = runTest {
        assertThrows<InternalServerException> {
            service.execute(
                MigrationLocalActor(1, 2),
                LocalUser(ActorId(1), UserDetailId(1), Acct("test", "example.com"))
            )
        }
    }

    @Test
    fun canMigrationがtrueならmoveToを書き込む() = runTest {
        val from = TestActorFactory.create(1)
        val to = TestActorFactory.create(2)
        val userDetail = UserDetail.create(
            UserDetailId(1),
            ActorId(1), UserDetailHashedPassword
                ("")
        )
        whenever(actorRepository.findById(ActorId(1))).doReturn(from)
        whenever(actorRepository.findById(ActorId(2))).doReturn(to)
        whenever(userDetailRepository.findById(UserDetailId(1))).doReturn(userDetail)

        whenever(
            localActorMigrationCheckDomainService.canAccountMigration(
                userDetail,
                from,
                to
            )
        ).doReturn(AccountMigrationCheck.CanAccountMigration())

        service.execute(
            MigrationLocalActor(1, 2),
            LocalUser(ActorId(1), UserDetailId(1), Acct("test", "example.com"))
        )

        argumentCaptor<Actor> {
            verify(actorRepository, times(1)).save(capture())
            val first = allValues.first()

            assertEquals(first.moveTo, to.id)
        }
    }

    @Test
    fun canMigrationがfalseなら例外() = runTest {
        val from = TestActorFactory.create(1)
        val to = TestActorFactory.create(2)
        val userDetail = UserDetail.create(
            UserDetailId(1),
            ActorId(1), UserDetailHashedPassword
                ("")
        )

        whenever(actorRepository.findById(ActorId(1))).doReturn(from)
        whenever(actorRepository.findById(ActorId(2))).doReturn(to)
        whenever(userDetailRepository.findById(UserDetailId(1))).doReturn(userDetail)
        whenever(
            localActorMigrationCheckDomainService.canAccountMigration(
                userDetail,
                from,
                to
            )
        ).doReturn(
            AccountMigrationCheck.AlreadyMoved("Message"),
            AccountMigrationCheck.CircularReferences("Message"),
            AccountMigrationCheck.SelfReferences(),
            AccountMigrationCheck.AlsoKnownAsNotFound("Message"),
            AccountMigrationCheck.MigrationCoolDown("Message")
        )

        repeat(5) {
            assertThrows<IllegalArgumentException> {
                service.execute(
                    MigrationLocalActor(1, 2),
                    LocalUser(ActorId(1), UserDetailId(1), Acct("test", "example.com"))
                )
            }
        }

        verify(actorRepository, never()).save(any())
    }
}