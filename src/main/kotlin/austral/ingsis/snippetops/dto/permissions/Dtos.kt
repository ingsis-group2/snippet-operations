package austral.ingsis.snippetops.dto.permissions

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
    val user: User,
    val name: String,
    val language: String,
    val extension: String,
    val readers: List<User>,
    val content: String,
    val creationDate: LocalDateTime,
    val updateDate: LocalDateTime?,
)

data class SnippetLocation(
    val id: Long,
    val container: String,
)

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

data class SnippetGetterForm(
    val userId: String,
    val page: Int,
    val size: Int,
)

data class NewReaderForm(
    val snippetId: Long,
    val userId: String,
    val readerId: String,
)

data class SnippetUpdateDTO(val content: String)
data class SnippetUpdateDTO(
    val content: String,
)

data class LintStatusForm(
    val snippetId: Long,
)

data class SnippetLintStatusDTO(
    val id: Long,
    val snippetId: Long,
    val status: String,
)

data class UpdateLintStatusDTO(
    val snippetId: Long,
    val reportList: List<String>,
    val errorList: List<String>,
)

data class User(
    val id: String,
    val username: String,
    val email: String,
)
