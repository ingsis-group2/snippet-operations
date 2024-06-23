package austral.ingsis.snippetops.dto

import java.time.LocalDateTime

data class SnippetPermissionsDTO(
    val id: Long,
    val container: String,
    val writer: String,
    val name: String,
    val language: String,
    val extension: String,
    val readers: List<String>,
    val creationDate: LocalDateTime,
    val updateDate: LocalDateTime?,
)

data class SnippetDTO(
    val id: Long,
    val writer: String,
    val name: String,
    val language: String,
    val extension: String,
    val readers: List<String>,
    val content: String,
    val creationDate: LocalDateTime,
    val updateDate: LocalDateTime?,
)

data class SnippetLocation(val id: Long, val container: String)

data class SnippetCreate(
    val name: String,
    val language: String,
    val extension: String,
    val content: String,
)

data class SnippetPermissionsCreate(
    val writer: String,
    val name: String,
    val language: String,
    val extension: String,
    val content: String,
)

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

data class SnippetGetForm(
    val userId: String,
    val page: Int,
    val size: Int
)

data class SnippetUpdate(val id: Int, val content: String)
