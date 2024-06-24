package austral.ingsis.snippetops.dto.runner.test

data class RunnerTestDTO(
    val content: String,
    val version: String,
    val inputs: List<String>,
    val envs: Map<String, Any>,
    val expectedOutput: List<String>,
)
