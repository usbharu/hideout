package dev.usbharu.hideout.core.domain.service.actor.local

import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.actor.TestActorFactory
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetail
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailHashedPassword
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailId
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.time.Duration.Companion.days
import kotlin.time.toJavaDuration

class LocalActorMigrationCheckDomainServiceImplTest {

    @Test
    fun 最終お引越しから30日以内だと失敗() = runTest {
        val from = TestActorFactory.create()
        val to = TestActorFactory.create()
        val userDetail = UserDetail.create(
            UserDetailId(1),
            ActorId(1), UserDetailHashedPassword("")
        )
        userDetail.lastMigration = Instant.now().minusSeconds(100)

        val localActorMigrationCheckDomainServiceImpl = LocalActorMigrationCheckDomainServiceImpl()

        val canAccountMigration = localActorMigrationCheckDomainServiceImpl.canAccountMigration(userDetail, to, from)

        assertInstanceOf(AccountMigrationCheck.MigrationCoolDown::class.java, canAccountMigration)
    }

    @Test
    fun 自分自身に引っ越しできない(): Unit = runTest {

        val from = TestActorFactory.create()
        val to = TestActorFactory.create()
        val userDetail = UserDetail.create(
            UserDetailId(1),
            ActorId(1), UserDetailHashedPassword("")
        )
        val localActorMigrationCheckDomainServiceImpl = LocalActorMigrationCheckDomainServiceImpl()

        val canAccountMigration = localActorMigrationCheckDomainServiceImpl.canAccountMigration(userDetail, from, from)

        assertInstanceOf(AccountMigrationCheck.SelfReferences::class.java, canAccountMigration)
    }

    @Test
    fun 引越し先が引っ越している場合は引っ越しできない(): Unit = runTest {

        val from = TestActorFactory.create()
        val to = TestActorFactory.create(moveTo = 100)
        val userDetail = UserDetail.create(
            UserDetailId(1),
            ActorId(1), UserDetailHashedPassword("")
        )
        val localActorMigrationCheckDomainServiceImpl = LocalActorMigrationCheckDomainServiceImpl()

        val canAccountMigration = localActorMigrationCheckDomainServiceImpl.canAccountMigration(userDetail, from, to)

        assertInstanceOf(AccountMigrationCheck.AlreadyMoved::class.java, canAccountMigration)
    }

    @Test
    fun 自分自身が引っ越している場合は引っ越しできない() = runTest {
        val from = TestActorFactory.create(moveTo = 100)
        val to = TestActorFactory.create()
        val userDetail = UserDetail.create(
            UserDetailId(1),
            ActorId(1), UserDetailHashedPassword("")
        )
        val localActorMigrationCheckDomainServiceImpl = LocalActorMigrationCheckDomainServiceImpl()

        val canAccountMigration = localActorMigrationCheckDomainServiceImpl.canAccountMigration(userDetail, from, to)

        assertInstanceOf(AccountMigrationCheck.AlreadyMoved::class.java, canAccountMigration)
    }

    @Test
    fun 引越し先のalsoKnownAsに引越し元が含まれてない場合失敗する() = runTest {
        val from = TestActorFactory.create()
        val to = TestActorFactory.create(alsoKnownAs = setOf(ActorId(100)))
        val userDetail = UserDetail.create(
            UserDetailId(1),
            ActorId(1), UserDetailHashedPassword("")
        )
        val localActorMigrationCheckDomainServiceImpl = LocalActorMigrationCheckDomainServiceImpl()

        val canAccountMigration = localActorMigrationCheckDomainServiceImpl.canAccountMigration(userDetail, from, to)

        assertInstanceOf(AccountMigrationCheck.AlsoKnownAsNotFound::class.java, canAccountMigration)
    }

    @Test
    fun 正常に設定されている場合は成功する() = runTest {
        val from = TestActorFactory.create()
        val to = TestActorFactory.create(alsoKnownAs = setOf(from.id, ActorId(100)))
        val userDetail = UserDetail.create(
            UserDetailId(1),
            ActorId(1), UserDetailHashedPassword("")
        )
        val localActorMigrationCheckDomainServiceImpl = LocalActorMigrationCheckDomainServiceImpl()

        val canAccountMigration = localActorMigrationCheckDomainServiceImpl.canAccountMigration(userDetail, from, to)

        assertInstanceOf(AccountMigrationCheck.CanAccountMigration::class.java, canAccountMigration)
    }

    @Test
    fun お引越し履歴があっても30日以上経っていたら成功する() = runTest {
        val from = TestActorFactory.create()
        val to = TestActorFactory.create(alsoKnownAs = setOf(from.id, ActorId(100)))
        val userDetail = UserDetail.create(
            UserDetailId(1),
            ActorId(1), UserDetailHashedPassword("")
        )
        userDetail.lastMigration = Instant.now().minus(31.days.toJavaDuration())
        val localActorMigrationCheckDomainServiceImpl = LocalActorMigrationCheckDomainServiceImpl()

        val canAccountMigration = localActorMigrationCheckDomainServiceImpl.canAccountMigration(userDetail, from, to)

        assertInstanceOf(AccountMigrationCheck.CanAccountMigration::class.java, canAccountMigration)
    }
}