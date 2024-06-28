package austral.ingsis.snippetops.service

import austral.ingsis.snippetops.dto.permissions.SnippetDTO
import austral.ingsis.snippetops.dto.runner.execute.ExecutionOutputDTO
import austral.ingsis.snippetops.dto.runner.execute.RunnerExecutionDTO
import austral.ingsis.snippetops.dto.runner.format.FormatOutputDTO
import austral.ingsis.snippetops.dto.runner.format.RunnerFormatDTO
import austral.ingsis.snippetops.dto.runner.lint.LintOutputDTO
import austral.ingsis.snippetops.dto.runner.lint.RunnerLintDTO
import austral.ingsis.snippetops.dto.runner.test.RunnerTestDTO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class RunnerService(
    @Value("\${spring.services.snippet.runner}") val url: String,
    @Autowired val restTemplate: RestTemplate,
    @Autowired val snippetService: SnippetService,
) {
    fun executeSnippet(dto: RunnerExecutionDTO): ResponseEntity<ExecutionOutputDTO> {
        return try {
            restTemplate.postForEntity("$url/execute", dto, ExecutionOutputDTO::class.java)
        } catch (e: Exception) {
            ResponseEntity.status(500).body(ExecutionOutputDTO(emptyList(), listOf(e.message ?: "Internal server error")))
        }
    }

    fun formatSnippet(
        dto: RunnerFormatDTO,
        snippet: SnippetDTO,
        userId: String,
    ): ResponseEntity<FormatOutputDTO> {
        return try {
            // update the snippet content with the formatted content
            val response = restTemplate.postForEntity("$url/format", dto, FormatOutputDTO::class.java)
            if (response.statusCode.is2xxSuccessful) {
                snippetService.updateSnippet(
                    snippet.id,
                    response.body?.formattedCode ?: snippet.content,
                    userId,
                )
            }
            response
        } catch (e: Exception) {
            ResponseEntity.status(500).body(FormatOutputDTO("", listOf(e.message ?: "Internal server error")))
        }
    }

    fun lintSnippet(dto: RunnerLintDTO): ResponseEntity<LintOutputDTO> {
        return try {
            restTemplate.postForEntity("$url/lint", dto, LintOutputDTO::class.java)
        } catch (e: Exception) {
            ResponseEntity.status(500).body(LintOutputDTO(emptyList(), listOf(e.message ?: "Internal server error")))
        }
    }

    fun executeTestCase(dto: RunnerTestDTO): ResponseEntity<Boolean> {
        return try {
            restTemplate.postForEntity("$url/test", dto, Boolean::class.java)
        } catch (e: Exception) {
            ResponseEntity.status(500).body(false)
        }
    }
}
