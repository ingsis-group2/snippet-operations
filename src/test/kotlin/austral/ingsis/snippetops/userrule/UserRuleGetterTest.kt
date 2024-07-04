package austral.ingsis.snippetops.userrule

import austral.ingsis.snippetops.dto.permissions.User
import austral.ingsis.snippetops.redis.producer.FormatterRequestProducer
import austral.ingsis.snippetops.redis.producer.LintRequestProducer
import austral.ingsis.snippetops.repository.BucketRepository
import austral.ingsis.snippetops.service.SnippetService
import austral.ingsis.snippetops.service.UserRuleService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.util.Optional

class UserRuleGetterTest {
    private val bucketRepository: BucketRepository = mockk()
    private val snippetService: SnippetService = mockk()
    private val lintRequestProducer: LintRequestProducer = mockk()
    private val formatterRequestProducer: FormatterRequestProducer = mockk()
    private val userRuleService: UserRuleService =
        UserRuleService(
            bucketRepository,
            snippetService,
            lintRequestProducer,
            formatterRequestProducer,
        )
    private val user = User("auth0|123456789", "Tista", "tista@mail.com")
    private val container = "lint"
    private val fakeRules =
        mapOf(
            "rule1" to "una regla numero 1",
            "rule2" to "una regla numero 2",
        )
    private val defaultRules =
        mapOf(
            "enablePrintExpressions" to true,
            "caseConvention" to "CAMEL_CASE",
        )

    @Test
    fun `should success getting a user rule`() {
        val slicedId = user.id.substring(6, user.id.length)
        every { bucketRepository.get(slicedId, container, Map::class.java) } returns Optional.of(fakeRules)

        val response = this.userRuleService.getUserRules(user.id, container)
        assertEquals(HttpStatus.OK, response.statusCode)
    }

    @Test
    fun `should success getting rules when user did not set them`() {
        val slicedId = user.id.substring(6, user.id.length)
        every { bucketRepository.get(slicedId, container, Map::class.java) } returns Optional.empty()
        every { bucketRepository.save(slicedId, container, defaultRules, Map::class.java) } returns Optional.of(true)

        val response = this.userRuleService.getUserRules(user.id, container)
        assertEquals(HttpStatus.OK, response.statusCode)
        val responseBody = response.body as Map<*, *>
        assertEquals(responseBody.size, 2)
        assertTrue(responseBody.containsKey("enablePrintExpressions"))
        assertTrue(responseBody.containsKey("caseConvention"))
    }

    @Test
    fun `should fail with status 500 because asset service did not response`() {
        val slicedId = user.id.substring(6, user.id.length)
        every { bucketRepository.get(slicedId, container, Map::class.java) } throws RuntimeException("A scandalous error")

        val response = this.userRuleService.getUserRules(user.id, container)
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
    }
}
