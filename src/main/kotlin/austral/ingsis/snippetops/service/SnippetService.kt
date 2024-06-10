package austral.ingsis.snippetops.service

import austral.ingsis.snippetops.dto.SnippetCreate
import austral.ingsis.snippetops.dto.SnippetDTO
import austral.ingsis.snippetops.dto.SnippetPermissionsDTO
import austral.ingsis.snippetops.repository.BucketRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
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
    fun createSnippet(body: SnippetCreate): ResponseEntity<Boolean> {
    val headers = HttpHeaders().apply {
        contentType = MediaType.APPLICATION_JSON
    }
    val requestEntity = HttpEntity(body, headers)
    
    val snippetResponse: ResponseEntity<SnippetPermissionsDTO> = try {
        restTemplate.exchange("$url/snippet", HttpMethod.POST, requestEntity, SnippetPermissionsDTO::class.java)
    } catch (ex: HttpClientErrorException) {
        return ResponseEntity.status(ex.statusCode).build()
    }
    
    val snippet = snippetResponse.body ?: return ResponseEntity.badRequest().build()
    
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
    }
    fun getSnippet(id: String): ResponseEntity<Any> {
        val snippet =
            try {
                val response = restTemplate.exchange("$url/snippet?id=$id", HttpMethod.GET, null, SnippetPermissionsDTO::class.java)
                response
            } catch (ex: HttpClientErrorException) {
                ResponseEntity.status(ex.statusCode).build()
            }
        snippet as SnippetPermissionsDTO
        val content = this.bucketRepository.get(snippet.id.toString(), snippet.container)
        return when {
            content.isPresent ->
                ResponseEntity.ok()
                    .body(this.snippetDTO(snippet, content.get() as String))
            else -> ResponseEntity.notFound().build()
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
