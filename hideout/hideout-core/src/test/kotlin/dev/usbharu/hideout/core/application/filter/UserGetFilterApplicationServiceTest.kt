package dev.usbharu.hideout.core.application.filter

import dev.usbharu.hideout.core.application.exception.PermissionDeniedException
import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.filter.*
import dev.usbharu.hideout.core.domain.model.filter.Filter
import dev.usbharu.hideout.core.domain.model.filter.FilterKeyword
import dev.usbharu.hideout.core.domain.model.support.acct.Acct
import dev.usbharu.hideout.core.domain.model.support.principal.LocalUser
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailId
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import utils.TestTransaction

@ExtendWith(MockitoExtension::class)
class UserGetFilterApplicationServiceTest {
    @InjectMocks
    lateinit var service: UserGetFilterApplicationService

    @Mock
    lateinit var filterRepository: FilterRepository

    @Spy
    val transaction = TestTransaction

    @Test
    fun オーナーのみ取得できる() = runTest {
        val filter = Filter(
            FilterId(1), UserDetailId(1), FilterName("filter"), setOf(FilterContext.HOME), FilterAction.HIDE, setOf(
                FilterKeyword(
                    FilterKeywordId(1), FilterKeywordKeyword("aaa"), FilterMode.NONE
                )
            )
        )
        whenever(filterRepository.findByFilterId(FilterId(1))).doReturn(filter)

        service.execute(
            GetFilter(1), LocalUser(
                ActorId(1), UserDetailId(1),
                Acct("test", "example.com")
            )
        )
    }

    @Test
    fun オーナー以外は失敗() = runTest {
        val filter = Filter(
            FilterId(1), UserDetailId(1), FilterName("filter"), setOf(FilterContext.HOME), FilterAction.HIDE, setOf(
                FilterKeyword(
                    FilterKeywordId(1), FilterKeywordKeyword("aaa"), FilterMode.NONE
                )
            )
        )
        whenever(filterRepository.findByFilterId(FilterId(1))).doReturn(filter)


        assertThrows<PermissionDeniedException> {
            service.execute(
                GetFilter(1), LocalUser(
                    ActorId(3), UserDetailId(3),
                    Acct("test", "example.com")
                )
            )
        }
    }

    @Test
    fun フィルターが見つからない場合失敗() = runTest {
        assertThrows<IllegalArgumentException> {
            service.execute(
                GetFilter(1), LocalUser(
                    ActorId(3), UserDetailId(3),
                    Acct("test", "example.com")
                )
            )
        }
    }
}