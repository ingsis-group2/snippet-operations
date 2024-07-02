package austral.ingsis.snippetops.logging

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException
import java.util.UUID

@Component
class CorrelationIdFilter : OncePerRequestFilter() {
    companion object {
        private const val CORRELATION_ID_KEY = "correlation-id"
        private const val CORRELATION_ID_HEADER = "X-Correlation-Id"
    }

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        // MDC is propagated through logging events, so you can use it to store the correlation id.
        val correlationId = request.getHeader(CORRELATION_ID_HEADER) ?: UUID.randomUUID().toString()
        MDC.put(CORRELATION_ID_KEY, correlationId)
        try {
            filterChain.doFilter(request, response)
        } finally {
            MDC.remove(CORRELATION_ID_KEY)
        }
    }
}
