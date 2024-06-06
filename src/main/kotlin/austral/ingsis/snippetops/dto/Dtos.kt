package austral.ingsis.snippetops.dto

data class SnippetDTO(
    val id: String,
    val name: String,
    val language: String,
    val writer: String,
    val shared: List<String>,
)

data class SnippetLocation(val id: String, val container: String)

data class SnippetCreate(val writer: String)

data class SnippetShow(
    val id: String,
    val name: String,
    val writer: String,
    val content: String,
    val language: String,
    val extension: String,
    val creationDate: String,
    val updateDate: String?,
)

data class SnippetUpdate(val id: Int, val content: String)
