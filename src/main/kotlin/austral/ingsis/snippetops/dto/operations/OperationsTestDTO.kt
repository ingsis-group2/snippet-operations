package austral.ingsis.snippetops.dto.operations

data class OperationsTestDTO(
    val snippetId: Long,
    val version: String,
    val inputs: List<String>,
    val envs: Map<String, Any>,
    val output: String,
)
