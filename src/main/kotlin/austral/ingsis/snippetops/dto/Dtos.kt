package austral.ingsis.snippetops.dto

import java.time.LocalDateTime

data class SnippetDTO(
    val id: Long,
    val writer: String,
    val name: String,
    val language: String,
    val extension: String,
    val content: String,
    val creationDate: LocalDateTime,
    val updateDate: LocalDateTime?,
)

data class SnippetPermissionDTO(
    val id: Long,
    val container: String,
    val writer: String,
    val name: String,
    val language: String,
    val extension: String,
    val creationDate: LocalDateTime,
    val updateDate: LocalDateTime?,
)

data class SnippetLocation(val id: String, val container: String)

data class SnippetCreate(
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

data class SnippetUpdate(val id: Int, val content: String)
