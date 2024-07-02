package austral.ingsis.snippetops.dto.runner.format

data class RunnerFormatDTO(
    val content: String,
    val version: String,
    val formatRules: Map<String, Any>,
    val language: String,
)
