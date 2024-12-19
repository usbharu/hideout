package dev.usbharu.hideout.core.domain.model.filter

import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetail
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailHashedPassword
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailId
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test

class FilterTest {
    @Test
    fun `setFilterKeywords 所有者のみ変更できる`() {
        val filter = Filter.create(
            id = FilterId(1),
            userDetailId = UserDetailId(1),
            name = FilterName("aiueo"),
            filterContext = setOf(),
            filterAction = FilterAction.HIDE,
            filterKeywords = setOf()
        )

        val userDetail = UserDetail.create(
            id = UserDetailId(1),
            actorId = ActorId(1),
            password = UserDetailHashedPassword(""),
            autoAcceptFolloweeFollowRequest = false,
            lastMigration = null,
            null
        )

        assertDoesNotThrow {
            filter.setFilterKeywords(
                setOf(
                    FilterKeyword(
                        FilterKeywordId(1),
                        FilterKeywordKeyword("keyword"),
                        FilterMode.NONE
                    )
                ), userDetail
            )
        }

    }

    @Test
    fun compileFilterで正規表現として表すことができるNONE() {
        val filter = Filter(
            id = FilterId(1),
            userDetailId = UserDetailId(1),
            name = FilterName("aiueo"),
            filterContext = setOf(),
            filterAction = FilterAction.HIDE,
            filterKeywords = setOf(
                FilterKeyword(
                    FilterKeywordId(1),
                    FilterKeywordKeyword("hoge"),
                    FilterMode.NONE
                )
            )
        )

        kotlin.test.assertEquals("(hoge)", filter.compileFilter().pattern)

    }

    @Test
    fun compileFilterで正規表現として表すことができるWHOLE_WORD() {
        val filter = Filter(
            id = FilterId(1),
            userDetailId = UserDetailId(1),
            name = FilterName("aiueo"),
            filterContext = setOf(),
            filterAction = FilterAction.HIDE,
            filterKeywords = setOf(
                FilterKeyword(
                    FilterKeywordId(1),
                    FilterKeywordKeyword("hoge"),
                    FilterMode.WHOLE_WORD
                )
            )
        )

        kotlin.test.assertEquals("\\b(hoge)\\b", filter.compileFilter().pattern)

    }

    @Test
    fun compileFilterで正規表現として表すことができるREGEX() {
        val filter = Filter(
            id = FilterId(1),
            userDetailId = UserDetailId(1),
            name = FilterName("aiueo"),
            filterContext = setOf(),
            filterAction = FilterAction.HIDE,
            filterKeywords = setOf(
                FilterKeyword(
                    FilterKeywordId(1),
                    FilterKeywordKeyword("hoge"),
                    FilterMode.REGEX
                )
            )
        )

        kotlin.test.assertEquals("(hoge)", filter.compileFilter().pattern)

    }

    @Test
    fun compileFilterで正規表現として表すことができる() {
        val filter = Filter(
            id = FilterId(1),
            userDetailId = UserDetailId(1),
            name = FilterName("aiueo"),
            filterContext = setOf(),
            filterAction = FilterAction.HIDE,
            filterKeywords = setOf(
                FilterKeyword(
                    FilterKeywordId(1),
                    FilterKeywordKeyword("hoge"),
                    FilterMode.WHOLE_WORD
                ),
                FilterKeyword(
                    FilterKeywordId(2),
                    FilterKeywordKeyword("hoge"),
                    FilterMode.REGEX
                ),
                FilterKeyword(
                    FilterKeywordId(3),
                    FilterKeywordKeyword("hoge"),
                    FilterMode.NONE
                )
            )
        )

        kotlin.test.assertEquals("\\b(hoge)\\b|(hoge)|(hoge)", filter.compileFilter().pattern)

    }
}

