package util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

fun objectMapper(): ObjectMapper {
    return jacksonObjectMapper()
}