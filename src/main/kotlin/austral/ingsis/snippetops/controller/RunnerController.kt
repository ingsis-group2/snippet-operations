package austral.ingsis.snippetops.controller

import austral.ingsis.snippetops.dto.runner.execute.ExecutionDTO
import austral.ingsis.snippetops.dto.runner.execute.ExecutionOutputDTO
import austral.ingsis.snippetops.dto.runner.execute.RunnerExecutionDTO
import austral.ingsis.snippetops.dto.runner.format.FormatDTO
import austral.ingsis.snippetops.dto.runner.format.FormatOutputDTO
import austral.ingsis.snippetops.dto.runner.format.RunnerFormatDTO
import austral.ingsis.snippetops.dto.runner.lint.LintDTO
import austral.ingsis.snippetops.dto.runner.lint.LintOutputDTO
import austral.ingsis.snippetops.dto.runner.lint.RunnerLintDTO
import austral.ingsis.snippetops.service.RunnerService
import austral.ingsis.snippetops.service.SnippetService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/runner")
class RunnerController(
    @Autowired val runnerService: RunnerService,
    @Autowired val snippetService: SnippetService,
) {
    @PostMapping("/execute/{id}")
    suspend fun executeSnippet(
        @PathVariable id: String,
        @RequestBody body: ExecutionDTO,
    ): ResponseEntity<ExecutionOutputDTO> {
        val snippet = snippetService.getSnippet(id).body ?: throw Exception("Snippet not found")
        val content = snippet.content
        return runnerService.executeSnippet(RunnerExecutionDTO(content, body.version, body.inputs))
    }

    @PostMapping("/format/{id}")
    suspend fun formatSnippet(
        @PathVariable id: String,
        @RequestBody body: FormatDTO,
    ): ResponseEntity<FormatOutputDTO> {
        val snippet = snippetService.getSnippet(id).body ?: throw Exception("Snippet not found")
        val content = snippet.content
        return runnerService.formatSnippet(RunnerFormatDTO(content, body.version, body.formatRules))
    }

    @PostMapping("/lint/{id}")
    suspend fun lint(
        @PathVariable id: String,
        @RequestBody body: LintDTO,
    ): ResponseEntity<LintOutputDTO> {
        val snippet = snippetService.getSnippet(id).body ?: throw Exception("Snippet not found")
        val content = snippet.content
        return runnerService.lintSnippet(RunnerLintDTO(content, body.version, body.lintRules))
    }
}
