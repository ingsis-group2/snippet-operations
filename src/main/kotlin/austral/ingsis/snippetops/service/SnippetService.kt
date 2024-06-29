package austral.ingsis.snippetops.service

import austral.ingsis.snippetops.dto.permissions.NewReaderForm
import austral.ingsis.snippetops.dto.permissions.SnippetCreate
import austral.ingsis.snippetops.dto.permissions.SnippetDTO
import austral.ingsis.snippetops.dto.permissions.SnippetGetterForm
import austral.ingsis.snippetops.dto.permissions.SnippetLocation
import austral.ingsis.snippetops.dto.permissions.SnippetPermissionsCreate
import austral.ingsis.snippetops.dto.permissions.SnippetPermissionsDTO
import austral.ingsis.snippetops.dto.permissions.User
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

@Service
class SnippetService(
    @Value("\${spring.services.snippet.permissions}") val url: String,
    @Autowired val bucketRepository: BucketRepository,
    @Autowired var restTemplate: RestTemplate,
    @Autowired val userService: UserService,
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
                val result = bucketRepository.save(snippet.id.toString(), snippet.container, body.content, String::class.java)
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
            val reader = this.userService.getUserByEmail(readerMail)
            if (reader != null) {
                val requestEntity = HttpEntity(NewReaderForm(snippetId, userId, reader.id))
                val response =
                    restTemplate.exchange(
                        "$url/snippet/addReader",
                        HttpMethod.POST,
                        requestEntity,
                        Boolean::class.java,
                    )
                if (response.statusCode == HttpStatus.OK) {
                    return ResponseEntity.ok().build()
                }
            }
            throw BadRequestException()
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
            val existingSnippet = this.getSnippet(id)
            if (existingSnippet.statusCode != HttpStatus.OK) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
            }

            val snippet = existingSnippet.body
            val result = this.bucketRepository.save(snippet?.id.toString(), "snippet", body, String::class.java)
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
            val content = this.bucketRepository.get(snippet.id.toString(), snippet.container, String::class.java)
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
            val permissionsResponse =
                restTemplate.exchange(
                    "$url/snippet/byWriter",
                    HttpMethod.POST,
                    requestEntity,
                    object : ParameterizedTypeReference<List<SnippetPermissionsDTO>>() {},
                )
            return this.generateGetterByResponse(permissionsResponse)
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
            val permissionsResponse =
                restTemplate.exchange(
                    "$url/snippet/byReader",
                    HttpMethod.POST,
                    requestEntity,
                    object : ParameterizedTypeReference<List<SnippetPermissionsDTO>>() {},
                )
            return this.generateGetterByResponse(permissionsResponse)
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
            val permissionsResponse =
                restTemplate.exchange(
                    "$url/snippet/byReaderAndWriter",
                    HttpMethod.POST,
                    requestEntity,
                    object : ParameterizedTypeReference<List<SnippetPermissionsDTO>>() {},
                )
            return this.generateGetterByResponse(permissionsResponse)
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

    fun getContent(
        key: String,
        container: String,
    ): ResponseEntity<String> {
        return ResponseEntity.ok(this.bucketRepository.get(key, container, String::class.java).get().toString())
    }

    fun saveContent(
        key: String,
        container: String,
        content: String,
    ): ResponseEntity<Any> {
        return ResponseEntity.ok(this.bucketRepository.save(container, key, content, String::class.java).get())
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

    private fun snippetDTO(
        snippet: SnippetPermissionsDTO,
        content: String,
    ): SnippetDTO {
        val user = this.userService.getUserById(snippet.writer)!!
        val readers = mutableListOf<User>()
        snippet.readers.forEach { r -> readers.add(this.userService.getUserById(r)!!) }
        return SnippetDTO(
            snippet.id,
            user,
            snippet.name,
            snippet.language,
            snippet.extension,
            readers.toList(),
            content,
            snippet.creationDate,
            snippet.updateDate,
        )
    }

    private fun mapSnippetsIntoDtos(snippets: List<SnippetPermissionsDTO>?): List<SnippetDTO> {
        val dtos = mutableListOf<SnippetDTO>()
        snippets?.forEach { s ->
            val content = this.bucketRepository.get(s.id.toString(), s.container, String::class.java).get()
            dtos.add(
                this.snippetDTO(s, content.toString()),
            )
        }
        return dtos.toList()
    }

    private fun generateGetterByResponse(response: ResponseEntity<List<SnippetPermissionsDTO>>): ResponseEntity<List<SnippetDTO>> {
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
    }
}
