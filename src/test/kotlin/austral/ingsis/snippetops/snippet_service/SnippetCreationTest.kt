package austral.ingsis.snippetops.snippet_service

import austral.ingsis.snippetops.dto.permissions.SnippetCreate
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
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpStatus
import org.springframework.web.client.RestTemplate
import java.time.LocalDateTime
import java.util.Optional

class SnippetCreationTest {

    private val bucketRepository: BucketRepository = mockk()
    private val restTemplate: RestTemplate = mockk()
    private val userService: UserService = mockk()
    private val snippetService = SnippetService("", bucketRepository, restTemplate, userService)
    private val body = SnippetCreate(
        name = "Test Snippet",
        language = "Kotlin",
        extension = ".kt",
        content = "println('Hello, World!')"
    )
    private val userId = "user123"
    private val snippetPermissionsDTO = SnippetPermissionsDTO(
        id = 1L,
        container = "container",
        writer = userId,
        name = body.name,
        language = body.language,
        extension = body.extension,
        readers = listOf(),
        creationDate = LocalDateTime.now(),
        updateDate = null
    )


    @Test
    fun `test createSnippet`() {
        every { snippetService.sendRequest(any(), HttpMethod.POST, any(), SnippetPermissionsDTO::class.java) } returns ResponseEntity(snippetPermissionsDTO, HttpStatus.CREATED)
        every { bucketRepository.save(any(), any(), any(), String::class.java) } returns Optional.of(true)
        every { userService.getUserById(userId) } returns User(userId, "", "")

        val response = snippetService.createSnippet(body, userId)
        assertEquals(HttpStatus.CREATED, response.statusCode)
    }

    @Test
    fun `should fail creating snippet because of invalid body by name is blank`() {
        val invalidBody = SnippetCreate(
            name = "    ",
            language = "Kotlin",
            extension = ".kt",
            content = "println('Hello, World!')"
        )

        val response = snippetService.createSnippet(invalidBody, userId)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun `should fail creating snippet because snippet permissions did not answer`() {
        every { snippetService.sendRequest(any(), HttpMethod.POST, any(), SnippetPermissionsDTO::class.java) } returns ResponseEntity(null, HttpStatus.INTERNAL_SERVER_ERROR)

        val response = snippetService.createSnippet(body, userId)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun `should fail creating snippet because asset service did not answer`() {
        every { snippetService.sendRequest(any(), HttpMethod.POST, any(), SnippetPermissionsDTO::class.java) } returns ResponseEntity(snippetPermissionsDTO, HttpStatus.CREATED)
        every { bucketRepository.save(any(), any(), any(), String::class.java) } returns Optional.empty()

        val response = snippetService.createSnippet(body, userId)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun `should fail creating snippet because asset service could not save content`() {
        every { snippetService.sendRequest(any(), HttpMethod.POST, any(), SnippetPermissionsDTO::class.java) } returns ResponseEntity(snippetPermissionsDTO, HttpStatus.CREATED)
        every { bucketRepository.save(any(), any(), any(), String::class.java) } returns Optional.of(false)

        val response = snippetService.createSnippet(body, userId)
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }
}