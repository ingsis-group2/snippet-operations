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
        @AuthenticationPrincipal user: Jwt,
    ): ResponseEntity<FormatOutputDTO> {
        val snippet = snippetService.getSnippet(id).body ?: throw Exception("Snippet not found")
        val content = snippet.content

        val rules = userRuleService.getUserFormattingRules(user.claims["sub"].toString())
        return runnerService.formatSnippet(RunnerFormatDTO(content, body.version, rules))
    }

    @PostMapping("/lint/{id}")
    suspend fun lint(
        @PathVariable id: String,
        @RequestBody body: LintDTO,
        @AuthenticationPrincipal user: Jwt,
    ): ResponseEntity<LintOutputDTO> {
        val snippet = snippetService.getSnippet(id).body ?: throw Exception("Snippet not found")
        val content = snippet.content

        val rules = userRuleService.getUserLintingRules(user.claims["sub"].toString())
        return runnerService.lintSnippet(RunnerLintDTO(content, body.version, rules))
    }

    @PostMapping("/test/{id}")
    suspend fun test(
        @PathVariable id: String,
        @RequestBody version: String,
        @AuthenticationPrincipal user: Jwt,
    ): ResponseEntity<Boolean> {
        // looks for test number {id}
        // val testCase = snippetService.getTestCase(id).body ?: throw Exception("Test not found")
        // looks for the snippet from the test
        // val snippet = snippetService.getSnippet(testCase.snippetId).body ?: throw Exception("Snippet not found")

//        return runnerService.executeTestCase(
//            RunnerTestCaseDTO(snippet.content, version, testCase.inputs, testCase.envs, testCase.expectedOutput),
//        )
        return ResponseEntity.ok(true)
    }
}
