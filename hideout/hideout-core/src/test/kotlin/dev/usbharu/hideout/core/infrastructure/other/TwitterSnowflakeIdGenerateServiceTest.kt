package dev.usbharu.hideout.core.infrastructure.other

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TwitterSnowflakeIdGenerateServiceTest {
    @Test
    fun noDuplicateTest() = runBlocking {

        val mutableListOf = coroutineScope {
            (1..10000).map {
                async {
                    TwitterSnowflakeIdGenerateService.generateId()
                }
            }.awaitAll()
        }

        assertEquals(0, mutableListOf.size - mutableListOf.toSet().size)
    }
}