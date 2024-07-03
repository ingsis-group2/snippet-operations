package austral.ingsis.snippetops.service.runner

import austral.ingsis.snippetops.dto.runner.lint.LintOutputDTO
import austral.ingsis.snippetops.dto.runner.lint.RunnerLintDTO
import austral.ingsis.snippetops.service.RunnerService
import austral.ingsis.snippetops.service.SnippetService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate

class RunnerLintingTest {

    private val restTemplate: RestTemplate = mockk()
    private val snippetService: SnippetService = mockk()
    private val runnerService = RunnerService("", restTemplate, snippetService)
    private val runnerLint =
        RunnerLintDTO(
            content = "Un contenido muy fachero",
            version = "1.0.0",
            lintRules = emptyMap(),
            language = "en-US",
        )
    private val lintOutput =
        LintOutputDTO(
            reportList = listOf("Reporto que este un snippet muy fachero", "Reporto que el reporte anterior tiene razón"),
            errors = emptyList(),
        )

    @Test
    fun `should success linting a snippet content`() {
        every {
            restTemplate.postForEntity(any<String>(), runnerLint, LintOutputDTO::class.java)
        } returns ResponseEntity(lintOutput, HttpStatus.OK)

        val response = runnerService.lintSnippet(runnerLint)
        assertEquals(HttpStatus.OK, response.statusCode)
    }

    @Test
    fun `should fail linting a snippet content because runner service failed linting it`() {
        val errorName = "An scandalous error"
        every {
            restTemplate.postForEntity(any<String>(), runnerLint, LintOutputDTO::class.java)
        } throws RuntimeException(errorName)

        val response = runnerService.lintSnippet(runnerLint)
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        val responseBody = response.body as LintOutputDTO
        assertTrue(responseBody.reportList.isEmpty())
        assertEquals(responseBody.errors.get(0), errorName)
    }

}