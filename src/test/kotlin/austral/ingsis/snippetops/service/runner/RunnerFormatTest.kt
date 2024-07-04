package austral.ingsis.snippetops.service.runner

import austral.ingsis.snippetops.dto.permissions.SnippetDTO
import austral.ingsis.snippetops.dto.permissions.User
import austral.ingsis.snippetops.dto.runner.format.FormatOutputDTO
import austral.ingsis.snippetops.dto.runner.format.RunnerFormatDTO
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
import java.time.LocalDateTime

class RunnerFormatTest {
    private val restTemplate: RestTemplate = mockk()
    private val snippetService: SnippetService = mockk()
    private val runnerService = RunnerService("", restTemplate, snippetService)
    private val user = User("TistaId", "Tista", "tista@mail.com")
    private val snippetDTO =
        SnippetDTO(
            id = 1L,
            user = user,
            name = "Nombre fachero",
            language = "spanish",
            extension = ".en",
            readers = emptyList(),
            content = "Contenido muy fachero",
            creationDate = LocalDateTime.now(),
            updateDate = null,
        )
    private val runnerFormat =
        RunnerFormatDTO(
            content = snippetDTO.content,
            version = "1.0",
            formatRules = emptyMap(),
            language = snippetDTO.language,
        )
    private val formatOutput =
        FormatOutputDTO(
            formattedCode = "Un formato que hace mucho más fachero al contenido muy fachero",
            errors = emptyList(),
        )

    @Test
    fun `should success formatting a snippet`() {
        every {
            restTemplate.postForEntity(any<String>(), runnerFormat, FormatOutputDTO::class.java)
        } returns ResponseEntity(formatOutput, HttpStatus.OK)
        val formattedContent = formatOutput.formattedCode
        every {
            snippetService.updateSnippet(snippetDTO.id, formattedContent, user.id)
        } returns ResponseEntity(HttpStatus.OK)

        val response = this.runnerService.formatSnippet(runnerFormat, snippetDTO, user.id)
        assertEquals(HttpStatus.OK, response.statusCode)
    }

    @Test
    fun `should fail formatting with status value 500 because snippet runner failed`() {
        every {
            restTemplate.postForEntity(any<String>(), runnerFormat, FormatOutputDTO::class.java)
        } throws RuntimeException("Internal server error")

        val response = this.runnerService.formatSnippet(runnerFormat, snippetDTO, user.id)
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        val responseBody = response.body as FormatOutputDTO
        assertTrue(responseBody.formattedCode.isBlank())
        assertTrue(responseBody.errors.isNotEmpty())
        assertEquals(responseBody.errors.get(0), "Internal server error")
    }
}
