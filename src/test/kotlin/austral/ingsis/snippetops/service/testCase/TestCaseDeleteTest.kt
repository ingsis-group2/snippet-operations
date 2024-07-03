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
import java.util.UUID
import java.util.Optional

class TestCaseDeleteTest {

    private val bucketRepository: BucketRepository = mockk()
    private val testCaseService = TestCaseService(bucketRepository)
    private val snippetId = 1L
    private val testCaseContainer = "test"
    private val testCaseIdContainer = "test-case-id"
    private val userId = "auth0|123456789"
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
    fun `should success with status OK by deleting a test case`() {
        val slicedId = userId.substring(6, userId.length)
        val testCaseIdsList = listOf(alreadyExistedTestCase.id)
        every { bucketRepository.get(slicedId, testCaseIdContainer, List::class.java) } returns Optional.of(testCaseIdsList)
        every { bucketRepository.save(slicedId, testCaseIdContainer, emptyList<String>(), List::class.java) } returns Optional.of(true)
        every { bucketRepository.delete(alreadyExistedTestCase.id, testCaseContainer) } returns Optional.of(true)

        val response = testCaseService.deleteTestCase(alreadyExistedTestCase.id, userId)
        assertEquals(HttpStatus.OK, response.statusCode)
        val responseBody = response.body
        assertNull(responseBody)
    }

    @Test
    fun `should fail with status Not_Found because asset service could not fetch ids data`() {
        val slicedId = userId.substring(6, userId.length)
        val testCaseIdsList = listOf(alreadyExistedTestCase.id)
        every { bucketRepository.get(slicedId, testCaseIdContainer, List::class.java) } returns Optional.empty()
        every { bucketRepository.delete(alreadyExistedTestCase.id, testCaseContainer) } returns Optional.of(true)

        val response = testCaseService.deleteTestCase(alreadyExistedTestCase.id, userId)
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        val responseBody = response.body
        assertNull(responseBody)
    }

    @Test
    fun `should fail with status Not_Found because asset service could not delete test case`() {
        val slicedId = userId.substring(6, userId.length)
        val testCaseIdsList = listOf(alreadyExistedTestCase.id)
        every { bucketRepository.get(slicedId, testCaseIdContainer, List::class.java) } returns Optional.of(testCaseIdsList)
        every { bucketRepository.save(slicedId, testCaseIdContainer, emptyList<String>(), List::class.java) } returns Optional.of(true)
        every { bucketRepository.delete(alreadyExistedTestCase.id, testCaseContainer) } returns Optional.empty()

        val response = testCaseService.deleteTestCase(alreadyExistedTestCase.id, userId)
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        val responseBody = response.body
        assertNull(responseBody)
    }
}