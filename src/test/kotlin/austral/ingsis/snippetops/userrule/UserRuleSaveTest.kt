package austral.ingsis.snippetops.userrule

import austral.ingsis.snippetops.dto.permissions.User
import austral.ingsis.snippetops.redis.producer.FormaterRequestProducer
import austral.ingsis.snippetops.redis.producer.LintRequestProducer
import austral.ingsis.snippetops.repository.BucketRepository
import austral.ingsis.snippetops.service.SnippetService
import austral.ingsis.snippetops.service.UserRuleService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.util.Optional

class UserRuleSaveTest {
    private val bucketRepository: BucketRepository = mockk()
    private val snippetService: SnippetService = mockk()
    private val lintRequestProducer: LintRequestProducer = mockk()
    private val formaterRequestProducer: FormaterRequestProducer = mockk()
    private val userRuleService: UserRuleService =
        UserRuleService(
            bucketRepository,
            snippetService,
            lintRequestProducer,
            formaterRequestProducer,
        )
    private val user = User("auth0|123456789", "Tista", "tista@mail.com")
    private val container = "lint"
    private val fakeUserRules =
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
    fun `should success saving rules from user`() {
        val slicedId = user.id.substring(6, user.id.length)
        every {
            bucketRepository.save(slicedId, container, fakeUserRules, Map::class.java)
        } returns Optional.of(true)

        val response = userRuleService.saveUserRules(user.id, fakeUserRules, container)
        assertEquals(HttpStatus.CREATED, response.statusCode)
    }

    @Test
    fun `should failed saving rules because asset service did not save it`() {
        val slicedId = user.id.substring(6, user.id.length)
        every {
            bucketRepository.save(slicedId, container, fakeUserRules, Map::class.java)
        } returns Optional.of(false)

        val response = userRuleService.saveUserRules(user.id, fakeUserRules, container)
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
    }

    @Test
    fun `should failed saving rules because asset service did not response`() {
        val slicedId = user.id.substring(6, user.id.length)
        every {
            bucketRepository.save(slicedId, container, fakeUserRules, Map::class.java)
        } returns Optional.empty()

        val response = userRuleService.saveUserRules(user.id, fakeUserRules, container)
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
    }
}
