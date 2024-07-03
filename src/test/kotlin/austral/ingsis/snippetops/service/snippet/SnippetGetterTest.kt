package austral.ingsis.snippetops.service.snippet

import austral.ingsis.snippetops.dto.permissions.*
import austral.ingsis.snippetops.repository.BucketRepository
import austral.ingsis.snippetops.service.SnippetService
import austral.ingsis.snippetops.service.UserService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.*
import org.springframework.web.client.RestTemplate
import java.time.LocalDateTime
import java.util.*

class SnippetGetterTest {

    private val bucketRepository: BucketRepository = mockk()
    private val restTemplate: RestTemplate = mockk()
    private val userService: UserService = mockk()
    private val url = "http://snippet-permissions:8080"
    private val snippetService = SnippetService(url, bucketRepository, restTemplate, userService)
    private val user = User("userId1234", "tista", "tista@mail.com")
    private val anotherUser = User("anotherUser", "papa", "papa@mail.com")
    private val firstSnippetWrittenByTistaPermissionsDTO = SnippetPermissionsDTO(
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
    private val secondSnippetWrittenByTistaPermissionsDTO = SnippetPermissionsDTO(
        id = 2L,
        container = "container",
        writer = user.id,
        name = "Tista second snippet",
        language = "Python",
        extension = ".py",
        readers = listOf(),
        creationDate = LocalDateTime.now(),
        updateDate = null
    )
    private val snippetThatTistaCanRead = SnippetPermissionsDTO(
        id = 10L,
        container = "container",
        writer = anotherUser.id,
        name = "A cool name",
        language = "Kotlin",
        extension = ".kt",
        readers = listOf("userId1234"),
        creationDate = LocalDateTime.now(),
        updateDate = null
    )

    @Test
    fun `should success with status OK and return a snippet by Id`() {
        val id = firstSnippetWrittenByTistaPermissionsDTO.id
        every {
            snippetService.sendRequest("http://snippet-permissions:8080/snippet/$id", HttpMethod.GET, null, SnippetPermissionsDTO::class.java)
        } returns ResponseEntity.ok(firstSnippetWrittenByTistaPermissionsDTO)
        every { bucketRepository.get(id.toString(), "container", String::class.java) } returns Optional.of("first snippet content")
        every { userService.getUserById("userId1234") } returns user

        val response = snippetService.getSnippet(id)

        assertEquals(HttpStatus.OK, response.statusCode)
        val snippetDTO = response.body
        assertEquals(firstSnippetWrittenByTistaPermissionsDTO.id, snippetDTO?.id)
        assertEquals("first snippet content", snippetDTO?.content)
        assertEquals(user.id, snippetDTO?.user?.id)
    }

    @Test
    fun `should success with status Not_Found`() {
        val nonExistentId = 3L
        every {
            snippetService.sendRequest("http://snippet-permissions:8080/snippet/$nonExistentId", HttpMethod.GET, null, SnippetPermissionsDTO::class.java)
        } returns ResponseEntity.notFound().build()

        val response = snippetService.getSnippet(nonExistentId)
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun `should success with status OK and return a list of snippets written by Tista`() {
        val page = 1
        val snippetPermissionsDTO = listOf(
            firstSnippetWrittenByTistaPermissionsDTO,
            secondSnippetWrittenByTistaPermissionsDTO
        )
        val requestEntity = HttpEntity(SnippetGetterForm(user.id, page, 10))

        every {
            restTemplate.exchange(
                "http://snippet-permissions:8080/snippet/byWriter",
                HttpMethod.POST,
                requestEntity,
                object : ParameterizedTypeReference<List<SnippetPermissionsDTO>>() {}
            )
        } returns ResponseEntity(snippetPermissionsDTO, HttpStatus.OK)
        every { bucketRepository.get("1", firstSnippetWrittenByTistaPermissionsDTO.container, String::class.java) } returns Optional.of("first snippet content")
        every { bucketRepository.get("2", secondSnippetWrittenByTistaPermissionsDTO.container, String::class.java) } returns Optional.of("second snippet content")
        every { userService.getUserById("userId1234") } returns user

        val response = snippetService.getSnippetByWriter(user.id, page)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(2, response.body?.size)
        val fistSnippet = response.body?.get(0)
        val secondSnippet = response.body?.get(1)

        assertEquals(firstSnippetWrittenByTistaPermissionsDTO.id, fistSnippet?.id)
        assertEquals(secondSnippetWrittenByTistaPermissionsDTO.id, secondSnippet?.id)
    }

    @Test
    fun `should success with status OK and return a list of snippets that Tista can read`() {
        val snippetPermissions = listOf(snippetThatTistaCanRead)
        val requestEntity = HttpEntity(SnippetGetterForm(user.id, 0, 10))

        every {
            restTemplate.exchange(
                "http://snippet-permissions:8080/snippet/byReader",
                HttpMethod.POST,
                requestEntity,
                object : ParameterizedTypeReference<List<SnippetPermissionsDTO>>() {}
            )
        } returns ResponseEntity(snippetPermissions, HttpStatus.OK)
        every {
            bucketRepository.get(snippetThatTistaCanRead.id.toString(), snippetThatTistaCanRead.container, String::class.java)
        } returns Optional.of("A content that tista can read")
        every { userService.getUserById(anotherUser.id) } returns anotherUser
        every { userService.getUserById(user.id) } returns user

        val response = snippetService.getSnippetByReader(user.id, 0)
        assertEquals(HttpStatus.OK, response.statusCode)
        val body = response.body
        assertEquals(snippetPermissions.size, body?.size)

        val readers = body?.get(0)?.readers as List<*>
        assertTrue(readers.contains(user))
        assertNotEquals(user.id, body[0].user.id)
    }

    @Test
    fun `should success with status OK and return a list of snippets that Tista wrote and can read`() {
        val snippetPermissions = listOf(
            firstSnippetWrittenByTistaPermissionsDTO,
            secondSnippetWrittenByTistaPermissionsDTO,
            snippetThatTistaCanRead,
        )
        val requestEntity = HttpEntity(SnippetGetterForm(user.id, 0, 10))

        every {
            restTemplate.exchange(
                "http://snippet-permissions:8080/snippet/byReaderAndWriter",
                HttpMethod.POST,
                requestEntity,
                object : ParameterizedTypeReference<List<SnippetPermissionsDTO>>() {}
            )
        } returns ResponseEntity(snippetPermissions, HttpStatus.OK)
        every {
            bucketRepository.get(firstSnippetWrittenByTistaPermissionsDTO.id.toString(), firstSnippetWrittenByTistaPermissionsDTO.container, String::class.java)
        } returns Optional.of("first snippet content")
        every {
            bucketRepository.get(secondSnippetWrittenByTistaPermissionsDTO.id.toString(), secondSnippetWrittenByTistaPermissionsDTO.container, String::class.java)
        } returns Optional.of("second snippet content")
        every {
            bucketRepository.get(snippetThatTistaCanRead.id.toString(), snippetThatTistaCanRead.container, String::class.java)
        } returns Optional.of("A content that tista can read")
        every { userService.getUserById(anotherUser.id) } returns anotherUser
        every { userService.getUserById(user.id) } returns user

        val response = snippetService.getSnippetByReaderAndWriter(user.id, 0)
        assertEquals(HttpStatus.OK, response.statusCode)
        val body = response.body
        assertEquals(snippetPermissions.size, body?.size)

        val firstSnippet = response.body?.get(0)
        val secondSnippet = response.body?.get(1)
        val thirdSnippet = response.body?.get(2)
        assertEquals(firstSnippetWrittenByTistaPermissionsDTO.id, firstSnippet?.id)
        assertEquals(secondSnippetWrittenByTistaPermissionsDTO.id, secondSnippet?.id)
        assertEquals(snippetThatTistaCanRead.id, thirdSnippet?.id)
    }
}