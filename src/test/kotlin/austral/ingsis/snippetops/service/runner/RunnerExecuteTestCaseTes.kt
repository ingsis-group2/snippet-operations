package austral.ingsis.snippetops.service.runner

import austral.ingsis.snippetops.dto.runner.test.RunnerTestDTO
import austral.ingsis.snippetops.service.RunnerService
import austral.ingsis.snippetops.service.SnippetService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate

class RunnerExecuteTestCaseTes {

    private val restTemplate: RestTemplate = mockk()
    private val snippetService: SnippetService = mockk()
    private val runnerService = RunnerService("", restTemplate, snippetService)
    private val runnerTest =
        RunnerTestDTO(
            content = "Un contenido fachero",
            version = "1.0.0",
            inputs = emptyList(),
            envs = emptyMap(),
            expectedOutput = listOf("Es un snippet poco fachero"),
            language = "spanish"
        )

    @Test
    fun `should success executing a test case of snippet`() {
        every {
            restTemplate.postForEntity(any<String>(), runnerTest, Boolean::class.java)
        } returns ResponseEntity(true, HttpStatus.OK)

        val response = runnerService.executeTestCase(runnerTest)
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
    }

    @Test
    fun `should fail executing test case of snippet because snippet runner failed`() {
        every {
            restTemplate.postForEntity(any<String>(), runnerTest, Boolean::class.java)
        } throws RuntimeException()

        val response = runnerService.executeTestCase(runnerTest)
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals(response.body, false)
    }
}