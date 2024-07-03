package austral.ingsis.snippetops.snippet_service

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

class SnippetAddReaderTest {

    private val bucketRepository: BucketRepository = mockk()
    private val restTemplate: RestTemplate = mockk()
    private val userService: UserService = mockk()
    private val snippetService = SnippetService("", bucketRepository, restTemplate, userService)
    private val userId = "user123"
    private val readerMail = "mail@hola.com"
    private val readerUser = User("readerId", "reader", readerMail)

    @Test
    fun `should success with OK status`() {
        every { snippetService.sendRequest(any(), HttpMethod.POST, any(), Boolean::class.java) } returns ResponseEntity.ok(true)
        every { userService.getUserByEmail(readerMail) } returns readerUser
        val response = snippetService.addNewReaderIntoSnippet(userId, readerMail, 1L)
        assertEquals(HttpStatus.OK, response.statusCode)
    }

    @Test
    fun `should fail with bad request because can not find user`() {
        every { userService.getUserByEmail(readerMail) } returns null  //user does not exist, so method returns null
        val response = snippetService.addNewReaderIntoSnippet(userId, readerMail, 1L)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun `should fail with bad request because snippet permissions failed in response`() {
        every { snippetService.sendRequest(any(), HttpMethod.POST, any(), Boolean::class.java) } returns ResponseEntity.internalServerError().build()
        every { userService.getUserByEmail(readerMail) } returns readerUser
        val response = snippetService.addNewReaderIntoSnippet(userId, readerMail, 1L)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }
}