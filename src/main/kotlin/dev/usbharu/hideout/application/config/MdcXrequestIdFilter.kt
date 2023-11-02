package dev.usbharu.hideout.application.config

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import org.slf4j.MDC
import org.springframework.boot.autoconfigure.security.SecurityProperties
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.util.*

@Component
@Order(SecurityProperties.DEFAULT_FILTER_ORDER - 1)
class MdcXrequestIdFilter : Filter {
    override fun doFilter(request: ServletRequest?, response: ServletResponse?, chain: FilterChain) {
        val uuid = UUID.randomUUID()
        try {
            MDC.put(KEY, uuid.toString())
            chain.doFilter(request, response)
        } finally {
            MDC.remove(KEY)
        }
    }

    companion object {
        private const val KEY = "x-request-id"
    }
}