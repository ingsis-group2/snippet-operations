package austral.ingsis.snippetops.service

import austral.ingsis.snippetops.dto.SnippetCreate
import austral.ingsis.snippetops.dto.SnippetDTO
import austral.ingsis.snippetops.dto.SnippetPermissionsCreate
import austral.ingsis.snippetops.dto.SnippetPermissionsDTO
import austral.ingsis.snippetops.repository.BucketRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate

@Service
class SnippetService(
    @Value("\${spring.services.snippet.permissions}") val url: String,
    @Autowired val bucketRepository: BucketRepository,
    @Autowired var restTemplate: RestTemplate,
) {
    fun createSnippet(
        body: SnippetCreate,
        userId: String,
    ): ResponseEntity<Boolean> {
        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }
        val snippetCreate =
            SnippetPermissionsCreate(
                userId,
                body.name,
                body.language,
                body.extension,
                body.content,
            )
        val requestEntity = HttpEntity(snippetCreate, headers)
        try {
            val snippetResponseEntity =
                try {
                    val response = restTemplate.exchange("$url/snippet", HttpMethod.POST, requestEntity, SnippetPermissionsDTO::class.java)
                    when {
                        response.statusCode == HttpStatus.CREATED -> response
                        else -> throw Exception()
                    }
                } catch (ex: HttpClientErrorException) {
                    return ResponseEntity.status(ex.statusCode).build()
                }
            val snippet = snippetResponseEntity.body ?: return ResponseEntity.badRequest().build()

            val result = bucketRepository.save(snippet.id.toString(), snippet.container, body.content)
            return if (result.isPresent) {
                if (result.get() == true) {
                    ResponseEntity(true, HttpStatus.CREATED)
                } else {
                    ResponseEntity.notFound().build()
                }
            } else {
                ResponseEntity.badRequest().build()
            }
        } catch (e: Exception) {
            return ResponseEntity.badRequest().build()
        }
    }

    fun getSnippet(id: String): ResponseEntity<Any> {
        try {
            val snippet =
                try {
                    val response = restTemplate.exchange("$url/snippet/$id", HttpMethod.GET, null, SnippetPermissionsDTO::class.java)
                    when {
                        response.statusCode == HttpStatus.NOT_FOUND -> throw NotFoundException()
                        response.statusCode == HttpStatus.INTERNAL_SERVER_ERROR -> throw InternalError()
                        else -> response
                    }
                } catch (ex: HttpClientErrorException) {
                    ResponseEntity.status(ex.statusCode).build()
                }
            snippet as SnippetPermissionsDTO
            val content = this.bucketRepository.get(snippet.id.toString(), snippet.container)
            return when {
                content.isPresent ->
                    ResponseEntity.ok()
                        .body(this.snippetDTO(snippet, content.get() as String))
                else -> throw NotFoundException()
            }
        } catch (e: NotFoundException) {
            return ResponseEntity.notFound().build()
        } catch (e: InternalError) {
            return ResponseEntity.badRequest().build()
        }
    }

    fun deleteSnippet(id: Long): ResponseEntity<Boolean> {
        try {
            val snippet =
                try {
                    val response = restTemplate.exchange("$url/snippet/$id", HttpMethod.DELETE, null, SnippetPermissionsDTO::class.java)
                    when {
                        response.statusCode == HttpStatus.NOT_FOUND -> throw NotFoundException()
                        response.statusCode == HttpStatus.INTERNAL_SERVER_ERROR -> throw InternalError()
                        else -> response
                    }
                } catch (ex: HttpClientErrorException) {
                    ResponseEntity.status(ex.statusCode).build()
                }
            snippet as SnippetPermissionsDTO
            val response = this.bucketRepository.delete(snippet.id.toString(), snippet.container)
            return when {
                response.isPresent -> ResponseEntity.ok().build()
                else -> throw NotFoundException()
            }
        } catch (e: NotFoundException) {
            return ResponseEntity.notFound().build()
        } catch (e: InternalError) {
            return ResponseEntity.badRequest().build()
        }
    }

    private fun snippetDTO(
        snippet: SnippetPermissionsDTO,
        content: String,
    ): SnippetDTO {
        return SnippetDTO(
            snippet.id,
            snippet.writer,
            snippet.name,
            snippet.language,
            snippet.extension,
            snippet.readers,
            content,
            snippet.creationDate,
            snippet.updateDate,
        )
    }
}
