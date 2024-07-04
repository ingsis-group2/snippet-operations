package austral.ingsis.snippetops.service.user

import austral.ingsis.snippetops.dto.permissions.User
import austral.ingsis.snippetops.service.UserService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate

class UserGetTest {
    private val restTemplate: RestTemplate = mockk()
    private val userService = UserService("", "", "", restTemplate)
    private val alreadyExistedUser = User("TistaId", "Tista", "Tista@mail.com")
    private val tokenResponse =
        ResponseEntity<Map<*, *>>(
            mapOf("access_token" to "this is a cool token"),
            HttpStatus.OK,
        )
    private val authResponseWithUser =
        ResponseEntity<Map<*, *>>(
            mapOf(
                "nickname" to alreadyExistedUser.username,
                "email" to alreadyExistedUser.email,
            ),
            HttpStatus.OK,
        )

    @Test
    fun `should success getting user by id`() {
        every {
            restTemplate.exchange(
                any<String>(),
                eq(HttpMethod.POST),
                any<HttpEntity<Map<String, String>>>(),
                eq(Map::class.java),
            )
        } returns tokenResponse
        every {
            restTemplate.exchange(
                any<String>(),
                HttpMethod.GET,
                any(),
                Map::class.java,
            )
        } returns authResponseWithUser

        val user = userService.getUserById(alreadyExistedUser.id)
        assertNotNull(user)
        assertEquals(user?.id, alreadyExistedUser.id)
        assertEquals(user?.username, alreadyExistedUser.username)
        assertEquals(user?.email, alreadyExistedUser.email)
    }

    @Test
    fun `should fail with null because service could not fetch access token`() {
        every {
            restTemplate.exchange(
                any<String>(),
                eq(HttpMethod.POST),
                any<HttpEntity<Map<String, String>>>(),
                eq(Map::class.java),
            )
        } returns ResponseEntity(null, HttpStatus.FORBIDDEN)

        val user = userService.getUserById(alreadyExistedUser.id)
        assertNull(user)
    }

    @Test
    fun `should success getting user by email`() {
        val userInResponse =
            mapOf(
                "user_id" to alreadyExistedUser.id,
                "nickname" to alreadyExistedUser.username,
                "email" to alreadyExistedUser.email,
            )
        val authResponseForCase = ResponseEntity<Array<*>>(arrayOf(userInResponse), HttpStatus.OK)
        every {
            restTemplate.exchange(
                any<String>(),
                eq(HttpMethod.POST),
                any<HttpEntity<Map<String, String>>>(),
                eq(Map::class.java),
            )
        } returns tokenResponse
        every {
            restTemplate.exchange(
                any<String>(),
                HttpMethod.GET,
                any(),
                Array::class.java,
            )
        } returns authResponseForCase

        val user = userService.getUserByEmail(alreadyExistedUser.email)
        assertNotNull(user)
        assertEquals(user?.id, alreadyExistedUser.id)
        assertEquals(user?.username, alreadyExistedUser.username)
        assertEquals(user?.email, alreadyExistedUser.email)
    }

    @Test
    fun `should fail getting user by mail with null because service could not fetch access token`() {
        every {
            restTemplate.exchange(
                any<String>(),
                eq(HttpMethod.POST),
                any<HttpEntity<Map<String, String>>>(),
                eq(Map::class.java),
            )
        } returns ResponseEntity(null, HttpStatus.FORBIDDEN)

        val user = userService.getUserByEmail(alreadyExistedUser.id)
        assertNull(user)
    }

    @Test
    fun `should success getting username by user Id`() {
        every {
            restTemplate.exchange(
                any<String>(),
                eq(HttpMethod.POST),
                any<HttpEntity<Map<String, String>>>(),
                eq(Map::class.java),
            )
        } returns tokenResponse
        every {
            restTemplate.exchange(
                any<String>(),
                HttpMethod.GET,
                any(),
                Map::class.java,
            )
        } returns authResponseWithUser

        val username = userService.getNicknameById(alreadyExistedUser.id)
        assertNotNull(username)
        assertEquals(alreadyExistedUser.username, username)
    }

    @Test
    fun `should fail getting username by user id with null because service could not fetch access token`() {
        every {
            restTemplate.exchange(
                any<String>(),
                eq(HttpMethod.POST),
                any<HttpEntity<Map<String, String>>>(),
                eq(Map::class.java),
            )
        } returns ResponseEntity(null, HttpStatus.FORBIDDEN)

        val username = userService.getNicknameById(alreadyExistedUser.id)
        assertNull(username)
    }

    @Test
    fun `should success getting all users`() {
        val anotherUser = User("SecondTistaId", "SecondTista", "secondTista@mail.com")
        val usersList =
            listOf(
                mapOf(
                    "user_id" to alreadyExistedUser.id,
                    "nickname" to alreadyExistedUser.username,
                    "email" to alreadyExistedUser.email,
                ),
                mapOf(
                    "user_id" to anotherUser.id,
                    "nickname" to anotherUser.username,
                    "email" to anotherUser.email,
                ),
            )
        val authResponse = ResponseEntity<List<Map<*, *>>>(usersList, HttpStatus.OK)

        every {
            restTemplate.exchange(
                any<String>(),
                eq(HttpMethod.POST),
                any<HttpEntity<Map<String, String>>>(),
                eq(Map::class.java),
            )
        } returns tokenResponse

        every {
            restTemplate.exchange(
                any<String>(),
                eq(HttpMethod.GET),
                any<HttpEntity<*>>(),
                object : ParameterizedTypeReference<List<Map<*, *>>>() {},
            )
        } returns authResponse

        val result = userService.getAllUsers(0, 10)

        assertEquals(2, result.size)
        assertEquals(alreadyExistedUser.id, result[0].id)
        assertEquals(alreadyExistedUser.username, result[0].username)
        assertEquals(alreadyExistedUser.email, result[0].email)
        assertEquals(anotherUser.id, result[1].id)
        assertEquals(anotherUser.username, result[1].username)
        assertEquals(anotherUser.email, result[1].email)
    }
}
