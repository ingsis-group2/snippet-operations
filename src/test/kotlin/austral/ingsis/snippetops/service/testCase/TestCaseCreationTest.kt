package austral.ingsis.snippetops.service.testCase

import austral.ingsis.snippetops.dto.operations.CreateTestCase
import austral.ingsis.snippetops.dto.operations.OperationsTestDTO
import austral.ingsis.snippetops.repository.BucketRepository
import austral.ingsis.snippetops.service.TestCaseService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.util.Optional
import java.util.UUID

class TestCaseCreationTest {
    private val bucketRepository: BucketRepository = mockk()
    private val testCaseService = TestCaseService(bucketRepository)
    private val snippetId = 1L
    private val testCaseContainer = "test"
    private val testCaseIdContainer = "test-case-id"
    private val userId = "auth0|123456789"
    private val body =
        CreateTestCase(
            name = "A cool name",
            snippetId = snippetId,
            version = "1.0",
            inputs = emptyList(),
            envs = emptyMap(),
            output = listOf("Expected output A", "Expected output B"),
        )
    private val alreadyExistedTestCase =
        OperationsTestDTO(
            id = UUID.randomUUID().toString(),
            name = "another cool name",
            snippetId = snippetId,
            version = "1.0",
            inputs = emptyList(),
            envs = emptyMap(),
            output = listOf("Expected output A", "Expected output B"),
        )

    @Test
    fun `should success with status created by creating a test case`() {
        val key = userId.substring(6, userId.length)
        every { bucketRepository.save(any(), testCaseContainer, any(), OperationsTestDTO::class.java) } returns Optional.of(true)
        every { bucketRepository.get(key, testCaseIdContainer, List::class.java) } returns Optional.empty()
        every { bucketRepository.save(key, testCaseIdContainer, any(), List::class.java) } returns Optional.of(true)

        val response = this.testCaseService.createTestCase(body, userId)
        assertEquals(HttpStatus.CREATED, response.statusCode)
        val responseBody = response.body
        assertNotNull(responseBody)
        responseBody!!
        assertEquals(responseBody.snippetId, snippetId)
        assertEquals(responseBody.name, body.name)
    }

    @Test
    fun `should fail with status bad request because asset service could not save id`() {
        val key = userId.substring(6, userId.length)
        every { bucketRepository.save(any(), testCaseContainer, any(), OperationsTestDTO::class.java) } returns Optional.of(true)
        every { bucketRepository.get(key, testCaseIdContainer, List::class.java) } returns Optional.empty()
        every { bucketRepository.save(key, testCaseIdContainer, any(), List::class.java) } returns Optional.empty()

        val response = this.testCaseService.createTestCase(body, userId)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun `should succes with status created by creating a another test case for same snippet`() {
        val key = userId.substring(6, userId.length)
        val testCaseIdList = listOf(this.alreadyExistedTestCase.id)
        every { bucketRepository.save(any(), testCaseContainer, any(), OperationsTestDTO::class.java) } returns Optional.of(true)
        every { bucketRepository.get(key, testCaseIdContainer, List::class.java) } returns Optional.of(testCaseIdList)
        every { bucketRepository.save(key, testCaseIdContainer, any(), List::class.java) } returns Optional.of(true)

        val response = this.testCaseService.createTestCase(body, userId)
        assertEquals(HttpStatus.CREATED, response.statusCode)
        val responseBody = response.body
        assertNotNull(responseBody)
        responseBody!!
        assertEquals(responseBody.snippetId, snippetId)
        assertEquals(responseBody.name, body.name)
    }
}
