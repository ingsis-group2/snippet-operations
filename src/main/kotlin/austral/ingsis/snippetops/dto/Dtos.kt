package austral.ingsis.snippetops.dto

data class SnippetDTO(
    val id: String,
    val name: String,
    val language: String,
    val writer: String,
    val shared: List<String>,
)

data class UserDTO(val id: String)

data class SnippetLocation(val id: String, val container: String)
