package dev.usbharu.hideout.core.domain.model.filter

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class FilterNameTest {
    @Test
    fun FilterNameがlength以上のときは無視される() {
        val filterName = FilterName("a".repeat(1000))

        assertEquals(FilterName.LENGTH, filterName.name.length)
    }
}