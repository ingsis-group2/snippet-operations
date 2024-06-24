package austral.ingsis.snippetops.service

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
) {
    fun executeSnippet(dto: RunnerExecutionDTO): ResponseEntity<ExecutionOutputDTO> {
        return restTemplate.postForEntity("$url/execute", dto, ExecutionOutputDTO::class.java)
    }

    fun formatSnippet(dto: RunnerFormatDTO): ResponseEntity<FormatOutputDTO> {
        return restTemplate.postForEntity("$url/format", dto, FormatOutputDTO::class.java)
    }

    fun lintSnippet(dto: RunnerLintDTO): ResponseEntity<LintOutputDTO> {
        return restTemplate.postForEntity("$url/lint", dto, LintOutputDTO::class.java)
    }

    fun executeTestCase(dto: RunnerTestDTO): ResponseEntity<ExecutionOutputDTO> {
        return restTemplate.postForEntity("$url/test", dto, ExecutionOutputDTO::class.java)
    }
}
