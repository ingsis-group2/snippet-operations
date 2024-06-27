package austral.ingsis.snippetops.service

import austral.ingsis.snippetops.dto.NewReaderForm
import austral.ingsis.snippetops.dto.SnippetCreate
import austral.ingsis.snippetops.dto.SnippetDTO
import austral.ingsis.snippetops.dto.SnippetGetterForm
import austral.ingsis.snippetops.dto.SnippetLocation
import austral.ingsis.snippetops.dto.SnippetPermissionsCreate
import austral.ingsis.snippetops.dto.SnippetPermissionsDTO
import austral.ingsis.snippetops.repository.BucketRepository
import org.apache.coyote.BadRequestException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
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
import java.io.IOException

@Service
class SnippetService(
    @Value("\${spring.services.snippet.permissions}") val url: String,
    @Value("\${okta.oauth2.issuer}") val uri: String,
    @Autowired val bucketRepository: BucketRepository,
    @Autowired var restTemplate: RestTemplate,
) {
    fun createSnippet(
        body: SnippetCreate,
        userId: String,
    ): ResponseEntity<SnippetDTO> {
        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }
        try {
            this.checkCreateBody(body)
            val snippetCreate = this.createSnippetForPermissions(body, userId)
            val requestEntity = HttpEntity(snippetCreate, headers)
            val permissionsResponse =
                restTemplate.exchange(
                    "$url/snippet",
                    HttpMethod.POST,
                    requestEntity,
                    SnippetPermissionsDTO::class.java,
                )
            if (permissionsResponse.statusCode != HttpStatus.CREATED) throw Exception()

            if (permissionsResponse.body != null) {
                val snippet = permissionsResponse.body as SnippetPermissionsDTO
                val result = bucketRepository.save(snippet.id.toString(), snippet.container, body.content)
                if (result.isPresent) {
                    return if (result.get() == true) {
                        ResponseEntity(this.snippetDTO(snippet, result.toString()), HttpStatus.CREATED)
                    } else {
                        ResponseEntity.notFound().build()
                    }
                } else {
                    this.deleteSnippet(snippet.id)
                    return ResponseEntity.status(HttpStatus.CONFLICT).build()
                }
            } else {
                throw Exception()
            }
        } catch (ex: HttpClientErrorException) {
            return ResponseEntity.status(ex.statusCode).build()
        } catch (e: Exception) {
            return ResponseEntity.badRequest().build()
        }
    }

    fun addNewReaderIntoSnippet(
        userId: String,
        readerMail: String,
        snippetId: Long,
    ): ResponseEntity<Boolean> {
        try {
            val readerId = this.getUserIdByEmail(readerMail, userId)
            val requestEntity = HttpEntity(NewReaderForm(snippetId, userId, readerId))
            val response =
                restTemplate.exchange(
                    "$url/snippet/addReader",
                    HttpMethod.POST,
                    requestEntity,
                    Boolean::class.java,
                )
            if (response.statusCode == HttpStatus.OK) {
                return ResponseEntity.ok().build()
            } else {
                throw BadRequestException()
            }
        } catch (ex: Exception) {
            return ResponseEntity.badRequest().build()
        }
    }

    fun updateSnippet(
        id: Long,
        body: String,
        userId: String,
    ): ResponseEntity<Boolean> {
        try {
            // Check if the snippet exists
            val existingSnippet = this.getSnippet(id)
            if (existingSnippet.statusCode != HttpStatus.OK) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
            }

            // Delete the existing snippet from the bucket
            val snippet = existingSnippet.body
            val deleteResponse = this.bucketRepository.delete(snippet?.id.toString(), "snippet")
            if (!deleteResponse.isPresent) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build()
            }

            // Put the new snippet in the same place
            val result = this.bucketRepository.save(snippet?.id.toString(), "snippet", body)
            if (!result.isPresent) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build()
            }

            return ResponseEntity.ok().build()
        } catch (ex: HttpClientErrorException) {
            return ResponseEntity.status(ex.statusCode).build()
        } catch (e: Exception) {
            return ResponseEntity.badRequest().build()
        }
    }

    fun getSnippet(id: Long): ResponseEntity<SnippetDTO> {
        try {
            val permissionResponse =
                try {
                    val response = restTemplate.exchange("$url/snippet/$id", HttpMethod.GET, null, SnippetPermissionsDTO::class.java)
                    when (response.statusCode) {
                        HttpStatus.NOT_FOUND -> throw NotFoundException()
                        HttpStatus.INTERNAL_SERVER_ERROR -> throw InternalError()
                        else -> response
                    }
                } catch (ex: HttpClientErrorException) {
                    ResponseEntity.status(ex.statusCode).build()
                }
            val snippet = permissionResponse.body as SnippetPermissionsDTO
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

    fun getSnippetByWriter(
        userId: String,
        page: Int,
    ): ResponseEntity<List<SnippetDTO>> {
        try {
            val snippetGetterForm = SnippetGetterForm(userId, page, 10)
            val requestEntity = HttpEntity(snippetGetterForm)

            val response =
                restTemplate.exchange(
                    "$url/snippet/byWriter",
                    HttpMethod.POST,
                    requestEntity,
                    object : ParameterizedTypeReference<List<SnippetPermissionsDTO>>() {},
                )
            if (response.body != null) {
                val responseBody = response.body
                return when (response.statusCode) {
                    HttpStatus.OK -> ResponseEntity(this.mapSnippetsIntoDtos(responseBody), HttpStatus.OK)
                    HttpStatus.BAD_REQUEST -> ResponseEntity.badRequest().build()
                    else -> ResponseEntity.notFound().build()
                }
            } else {
                throw NullPointerException("Response body from permissions is null")
            }
        } catch (e: NullPointerException) {
            return ResponseEntity(HttpStatus.CONFLICT)
        }
    }

    fun getSnippetByReader(
        userId: String,
        page: Int,
    ): ResponseEntity<List<SnippetDTO>> {
        try {
            val snippetGetterForm = SnippetGetterForm(userId, page, 10)
            val requestEntity = HttpEntity(snippetGetterForm)

            val response =
                restTemplate.exchange(
                    "$url/snippet/byWriter",
                    HttpMethod.POST,
                    requestEntity,
                    object : ParameterizedTypeReference<List<SnippetPermissionsDTO>>() {},
                )
            if (response.body != null) {
                val responseBody = response.body
                return when (response.statusCode) {
                    HttpStatus.OK -> ResponseEntity(this.mapSnippetsIntoDtos(responseBody), HttpStatus.OK)
                    HttpStatus.BAD_REQUEST -> ResponseEntity.badRequest().build()
                    else -> ResponseEntity.notFound().build()
                }
            } else {
                throw NullPointerException("Response body from permissions is null")
            }
        } catch (e: NullPointerException) {
            return ResponseEntity(HttpStatus.CONFLICT)
        }
    }

    fun getSnippetByReaderAndWriter(
        userId: String,
        page: Int,
    ): ResponseEntity<List<SnippetDTO>> {
        try {
            val snippetGetterForm = SnippetGetterForm(userId, page, 10)
            val requestEntity = HttpEntity(snippetGetterForm)

            val response =
                restTemplate.exchange(
                    "$url/snippet/byWriter",
                    HttpMethod.POST,
                    requestEntity,
                    object : ParameterizedTypeReference<List<SnippetPermissionsDTO>>() {},
                )
            if (response.body != null) {
                val responseBody = response.body
                return when (response.statusCode) {
                    HttpStatus.OK -> ResponseEntity(this.mapSnippetsIntoDtos(responseBody), HttpStatus.OK)
                    HttpStatus.BAD_REQUEST -> ResponseEntity.badRequest().build()
                    else -> ResponseEntity.notFound().build()
                }
            } else {
                throw NullPointerException("Response body from permissions is null")
            }
        } catch (e: NullPointerException) {
            return ResponseEntity(HttpStatus.CONFLICT)
        }
    }

    fun deleteSnippet(id: Long): ResponseEntity<Boolean> {
        try {
            val permissionResponse =
                try {
                    val response = restTemplate.exchange("$url/snippet/$id", HttpMethod.DELETE, null, SnippetLocation::class.java)
                    when (response.statusCode) {
                        HttpStatus.NOT_FOUND -> throw NotFoundException()
                        HttpStatus.INTERNAL_SERVER_ERROR -> throw InternalError()
                        else -> response
                    }
                } catch (ex: HttpClientErrorException) {
                    ResponseEntity.status(ex.statusCode).build()
                }
            if (permissionResponse.body == null) {
                throw NotFoundException()
            }
            val location = permissionResponse.body as SnippetLocation
            val response = this.bucketRepository.delete(location.id.toString(), location.container)
            return when {
                response.isPresent -> ResponseEntity.ok().build()
                else -> throw NotFoundException()
            }
        } catch (e: NullPointerException) {
            return ResponseEntity.notFound().build()
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

    private fun checkCreateBody(body: SnippetCreate) {
        if (
            body.content.isBlank() || body.name.isBlank() || body.language.isBlank() || body.extension.isBlank()
        ) {
            throw NullPointerException()
        }
    }

    private fun createSnippetForPermissions(
        body: SnippetCreate,
        userId: String,
    ): SnippetPermissionsCreate {
        return SnippetPermissionsCreate(
            userId,
            body.name,
            body.language,
            body.extension,
            body.content,
        )
    }

    private fun mapSnippetsIntoDtos(snippets: List<SnippetPermissionsDTO>?): List<SnippetDTO> {
        val dtos = mutableListOf<SnippetDTO>()
        snippets?.forEach { s ->
            val content = this.bucketRepository.get(s.id.toString(), s.container)
            dtos.add(
                this.snippetDTO(s, content.toString()),
            )
        }
        return dtos.toList()
    }

    fun getUserIdByEmail(
        accessToken: String,
        email: String,
    ): String {
        val url = uri + "api/v2/users-by-email?email=$email"
        val headers = HttpHeaders()
        headers.set("Authorization", "Bearer $accessToken")
        val entity = HttpEntity<String>(headers)
        val response = restTemplate.exchange(url, HttpMethod.GET, entity, Array<Auth0User>::class.java)
        val users = response.body ?: throw IOException("User not found")
        if (users.isEmpty()) throw IOException("User not found")
        return users[0].user_id
    }
}

data class Auth0User(val user_id: String)
