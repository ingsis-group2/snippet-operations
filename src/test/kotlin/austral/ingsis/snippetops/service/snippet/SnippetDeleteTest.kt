package austral.ingsis.snippetops.service.snippet

import austral.ingsis.snippetops.dto.permissions.SnippetLocation
import austral.ingsis.snippetops.dto.permissions.SnippetPermissionsDTO
import austral.ingsis.snippetops.dto.permissions.User
import austral.ingsis.snippetops.repository.BucketRepository
import austral.ingsis.snippetops.service.SnippetService
import austral.ingsis.snippetops.service.UserService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import java.time.LocalDateTime
import java.util.*

class SnippetDeleteTest {

    private val bucketRepository: BucketRepository = mockk()
    private val restTemplate: RestTemplate = mockk()
    private val userService: UserService = mockk()
    private val url = "http://snippet-permissions:8080"
    private val snippetService = SnippetService(url, bucketRepository, restTemplate, userService)
    private val user = User("userId1234", "tista", "tista@mail.com")
    private val snippet = SnippetPermissionsDTO(
        id = 1L,
        container = "container",
        writer = user.id,
        name = "Tista first snippet",
        language = "Kotlin",
        extension = ".kt",
        readers = listOf(),
        creationDate = LocalDateTime.now(),
        updateDate = null
    )
    private val snippetLocation = SnippetLocation(snippet.id, snippet.container)

    @Test
    fun `should success with status OK deleting a snippet by id`() {
        val id = snippet.id
        every {
            snippetService.sendRequest("http://snippet-permissions:8080/snippet/$id", HttpMethod.DELETE, null, SnippetLocation::class.java)
        } returns ResponseEntity.ok(snippetLocation)
        every {
            bucketRepository.delete(snippet.id.toString(), snippet.container)
        } returns Optional.of(HttpStatus.OK)

        val response = snippetService.deleteSnippet(id)
        assertEquals(HttpStatus.OK, response.statusCode)
    }

    @Test
    fun `should fail with status NOT_FOUND because permissions could not find it by id`() {
        val id = snippet.id
        every {
            snippetService.sendRequest("http://snippet-permissions:8080/snippet/$id", HttpMethod.DELETE, null, SnippetLocation::class.java)
        } returns ResponseEntity.notFound().build()

        val response = snippetService.deleteSnippet(id)
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun `should fail with status NOT_FOUND because permissions response with null body while finding by id`() {
        val id = snippet.id
        every {
            snippetService.sendRequest("http://snippet-permissions:8080/snippet/$id", HttpMethod.DELETE, null, SnippetLocation::class.java)
        } returns ResponseEntity.ok(null)

        val response = snippetService.deleteSnippet(id)
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun `should fail with status bad request because permissions failed with internal server error`() {
        val id = snippet.id
        every {
            snippetService.sendRequest("http://snippet-permissions:8080/snippet/$id", HttpMethod.DELETE, null, SnippetLocation::class.java)
        } returns ResponseEntity.internalServerError().build()

        val response = snippetService.deleteSnippet(id)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun `should fail with status Not_Found because assets service failed deleting snippet`() {
        val id = snippet.id
        every {
            snippetService.sendRequest("http://snippet-permissions:8080/snippet/$id", HttpMethod.DELETE, null, SnippetLocation::class.java)
        } returns ResponseEntity.ok(snippetLocation)
        every {
            bucketRepository.delete(snippet.id.toString(), snippet.container)
        } returns Optional.empty()

        val response = snippetService.deleteSnippet(id)
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }
}