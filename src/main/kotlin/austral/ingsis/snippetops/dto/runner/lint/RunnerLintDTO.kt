package austral.ingsis.snippetops.dto.runner.lint

data class RunnerLintDTO(
    val content: String,
    val version: String,
    val lintRules: Map<String, Any>,
    val language: String,
)
