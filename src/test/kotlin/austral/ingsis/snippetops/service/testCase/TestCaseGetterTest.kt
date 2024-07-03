package austral.ingsis.snippetops.service.testCase

import austral.ingsis.snippetops.dto.operations.OperationsTestDTO
import austral.ingsis.snippetops.repository.BucketRepository
import austral.ingsis.snippetops.service.TestCaseService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.util.Optional
import java.util.UUID

class TestCaseGetterTest {

    private val bucketRepository: BucketRepository = mockk()
    private val testCaseService = TestCaseService(bucketRepository)
    private val snippetId = 1L
    private val testCaseContainer = "test"
    private val alreadyExistedTestCase = OperationsTestDTO(
        id = UUID.randomUUID().toString(),
        name = "another cool name",
        snippetId = snippetId,
        version = "1.0",
        inputs = emptyList(),
        envs = emptyMap(),
        output = listOf("Expected output A", "Expected output B"),
    )

    @Test
    fun `should success with status OK getting a test case by id`() {
        every {
            bucketRepository.get(alreadyExistedTestCase.id, testCaseContainer, OperationsTestDTO::class.java)
        } returns Optional.of(alreadyExistedTestCase)

        val response = this.testCaseService.getTestCase(alreadyExistedTestCase.id)
        assertEquals(HttpStatus.OK, response.statusCode)
        val responseBody = response.body as OperationsTestDTO
        assertEquals(responseBody.name, alreadyExistedTestCase.name)
        assertEquals(responseBody.id, alreadyExistedTestCase.id)
    }

    @Test
    fun `should fail with status Not_Found because asset service failed with Not_Found status`() {
        every {
            bucketRepository.get(alreadyExistedTestCase.id, testCaseContainer, OperationsTestDTO::class.java)
        } returns Optional.empty()

        val response = this.testCaseService.getTestCase(alreadyExistedTestCase.id)
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        val responseBody = response.body
        assertNull(responseBody)
    }
}