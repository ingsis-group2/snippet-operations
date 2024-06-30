package austral.ingsis.snippetops.controller

import austral.ingsis.snippetops.dto.operations.OperationsTestDTO
import austral.ingsis.snippetops.dto.runner.execute.ExecutionDTO
import austral.ingsis.snippetops.dto.runner.execute.ExecutionOutputDTO
import austral.ingsis.snippetops.dto.runner.execute.RunnerExecutionDTO
import austral.ingsis.snippetops.dto.runner.format.FormatDTO
import austral.ingsis.snippetops.dto.runner.format.FormatOutputDTO
import austral.ingsis.snippetops.dto.runner.format.RunnerFormatDTO
import austral.ingsis.snippetops.dto.runner.lint.LintDTO
import austral.ingsis.snippetops.dto.runner.lint.LintOutputDTO
import austral.ingsis.snippetops.dto.runner.lint.RunnerLintDTO
import austral.ingsis.snippetops.dto.runner.test.RunnerTestDTO
import austral.ingsis.snippetops.service.RunnerService
import austral.ingsis.snippetops.service.SnippetService
import austral.ingsis.snippetops.service.TestCaseService
import austral.ingsis.snippetops.service.UserRuleService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
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
    @Autowired val userRuleService: UserRuleService,
    @Autowired val testCaseService: TestCaseService,
) {
    @PostMapping("/execute/{id}")
    suspend fun executeSnippet(
        @PathVariable id: Long,
        @RequestBody body: ExecutionDTO,
        @AuthenticationPrincipal user: Jwt,
    ): ResponseEntity<ExecutionOutputDTO> {
        val snippet = snippetService.getSnippet(id).body ?: throw Exception("Snippet not found")
        val content = snippet.content
        return runnerService.executeSnippet(RunnerExecutionDTO(content, body.version, body.inputs))
    }

    @PostMapping("/format/{id}")
    suspend fun formatSnippet(
        @PathVariable id: Long,
        @RequestBody body: FormatDTO,
        @AuthenticationPrincipal user: Jwt,
    ): ResponseEntity<FormatOutputDTO> {
        val snippet = snippetService.getSnippet(id).body ?: throw Exception("Snippet not found")
        val content = snippet.content

        val userId = user.claims["sub"].toString()

        val rules = userRuleService.getUserRules(user.claims["sub"].toString(), "format")
        val unwrappedRules = rules.body ?: throw Exception("Rules not found")
        return runnerService.formatSnippet(RunnerFormatDTO(content, body.version, unwrappedRules as Map<String, Any>), snippet, userId)
    }

    @PostMapping("/lint/{id}")
    suspend fun lint(
        @PathVariable id: Long,
        @RequestBody body: LintDTO,
        @AuthenticationPrincipal user: Jwt,
    ): ResponseEntity<LintOutputDTO> {
        val snippet = snippetService.getSnippet(id).body ?: throw Exception("Snippet not found")
        val content = snippet.content

        val rules = userRuleService.getUserRules(user.claims["sub"].toString(), "lint")
        val unwrappedRules = rules.body ?: throw Exception("Rules not found")
        return runnerService.lintSnippet(RunnerLintDTO(content, body.version, unwrappedRules as Map<String, Any>))
    }

    @PostMapping("/test/{testId}")
    suspend fun test(
        @PathVariable testId: String,
        @RequestBody version: String,
        @AuthenticationPrincipal user: Jwt,
    ): ResponseEntity<Boolean> {
        val testCase = testCaseService.getTestCase(testId).body as OperationsTestDTO
        val snippet = snippetService.getSnippet(testCase.snippetId).body ?: throw Exception("Snippet not found")
        val content = snippet.content
        return runnerService.executeTestCase(RunnerTestDTO(content, testCase.version, testCase.inputs, testCase.envs, testCase.output))
    }
}
