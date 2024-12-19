package dev.usbharu.hideout.core.domain.service.filter

import dev.usbharu.hideout.core.domain.model.filter.*
import dev.usbharu.hideout.core.domain.model.post.TestPostFactory
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailId
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class FilterDomainServiceTest {
    @Test
    fun apply_filterContextの適用範囲にフィルターが適用される() {
        val post = TestPostFactory.create()

        val domainService = FilterDomainService()
        val filter = Filter(
            FilterId(1),
            userDetailId = UserDetailId(1),
            FilterName("filter"),
            setOf(FilterContext.HOME),
            filterAction = FilterAction.HIDE,
            setOf(FilterKeyword(FilterKeywordId(1), FilterKeywordKeyword("test"), FilterMode.NONE))
        )
        val apply = domainService.apply(post, FilterContext.HOME, listOf(filter))
        assertEquals(1, apply.filterResults.size)
        assertEquals("test", apply.filterResults.first().matchedKeyword)
    }

    @Test
    fun apply_filterContextに当てはまらないならfilterResultsが空になる() {
        val post = TestPostFactory.create()

        val domainService = FilterDomainService()
        val filter = Filter(
            FilterId(1),
            userDetailId = UserDetailId(1),
            FilterName("filter"),
            setOf(FilterContext.PUBLIC),
            filterAction = FilterAction.HIDE,
            setOf(FilterKeyword(FilterKeywordId(1), FilterKeywordKeyword("test"), FilterMode.NONE))
        )
        val apply = domainService.apply(post, FilterContext.HOME, listOf(filter))
        assertEquals(0, apply.filterResults.size)
    }

    @Test
    fun overviewにも適用される() {
        val post = TestPostFactory.create(overview = "test")

        val domainService = FilterDomainService()
        val filter = Filter(
            FilterId(1),
            userDetailId = UserDetailId(1),
            FilterName("filter"),
            setOf(FilterContext.HOME),
            filterAction = FilterAction.HIDE,
            setOf(FilterKeyword(FilterKeywordId(1), FilterKeywordKeyword("test"), FilterMode.NONE))
        )
        val apply = domainService.apply(post, FilterContext.HOME, listOf(filter))
        assertEquals(2, apply.filterResults.size)
        assertEquals("test", apply.filterResults.first().matchedKeyword)
    }

    @Test
    fun applyAll_filterContextの適用範囲にフィルターが適用される() {
        val postList = listOf(
            TestPostFactory.create(),
            TestPostFactory.create(),
            TestPostFactory.create(content = "aaaaaaaaaa"),
            TestPostFactory.create(),
            TestPostFactory.create()
        )
        val filter = Filter(
            FilterId(1),
            userDetailId = UserDetailId(1),
            FilterName("filter"),
            setOf(FilterContext.HOME),
            filterAction = FilterAction.HIDE,
            setOf(FilterKeyword(FilterKeywordId(1), FilterKeywordKeyword("test"), FilterMode.NONE))
        )


        val filteredPosts = FilterDomainService().applyAll(postList, FilterContext.HOME, filters = listOf(filter))

        assertEquals(5, filteredPosts.size)
    }
}