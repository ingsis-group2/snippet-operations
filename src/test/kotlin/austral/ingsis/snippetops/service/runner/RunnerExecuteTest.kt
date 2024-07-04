package austral.ingsis.snippetops.service.runner

import austral.ingsis.snippetops.dto.runner.execute.ExecutionOutputDTO
import austral.ingsis.snippetops.dto.runner.execute.RunnerExecutionDTO
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

class RunnerExecuteTest {
    private val restTemplate: RestTemplate = mockk()
    private val snippetService: SnippetService = mockk()
    private val runnerService = RunnerService("", restTemplate, snippetService)
    private val runnerExecution =
        RunnerExecutionDTO(
            "contenido fachero",
            "1.0",
            emptyList(),
            "facheros",
        )
    private val executionOutput =
        ExecutionOutputDTO(
            listOf("Output del snippet fachero sin errores porque es muy fachero"),
            emptyList(),
        )

    @Test
    fun `should success with status OK while executing a snippet`() {
        every {
            restTemplate.postForEntity(any<String>(), runnerExecution, ExecutionOutputDTO::class.java)
        } returns ResponseEntity.ok(executionOutput)

        val response = runnerService.executeSnippet(runnerExecution)
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(executionOutput, response.body)
    }

    @Test
    fun `should return status 500 when an exception occurs`() {
        every {
            restTemplate.postForEntity(any<String>(), runnerExecution, ExecutionOutputDTO::class.java)
        } throws RuntimeException("Internal error")

        val response = runnerService.executeSnippet(runnerExecution)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertNotNull(response.body)
        assertEquals(listOf("Internal error"), response.body?.errors)
    }
}
