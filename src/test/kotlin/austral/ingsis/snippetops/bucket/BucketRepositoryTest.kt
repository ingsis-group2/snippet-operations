package austral.ingsis.snippetops.bucket

import austral.ingsis.snippetops.repository.BucketRepositoryImpl
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate

class BucketRepositoryTest {
    private val restTemplate: RestTemplate = mockk()
    private val url = "http://asset_service:8080/v1/asset"
    private val bucketRepository = BucketRepositoryImpl(url, restTemplate)

    private val snippetContainer = "snippet"
    private val snippet = 1L
    private val snippetContent = "Contenido de un snippet"

    @Test
    fun `should success with Optional true while saving a snippet content`() {
        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }
        val requestEntity = HttpEntity(snippetContent, headers)
        every {
            restTemplate.exchange(
                "http://asset_service:8080/v1/asset/$snippetContainer/$snippet",
                HttpMethod.POST,
                requestEntity,
                Void::class.java,
            )
        } returns ResponseEntity(HttpStatus.CREATED)

        val response = bucketRepository.save(snippet.toString(), snippetContainer, snippetContent, String::class.java)
        assertTrue(response.isPresent)
        assertEquals(response.get(), true)
    }

    @Test
    fun `should fail with Optional false while saved a snippet content because asset service failed`() {
        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }
        val requestEntity = HttpEntity(snippetContent, headers)
        every {
            restTemplate.exchange(
                "http://asset_service:8080/v1/asset/$snippetContainer/$snippet",
                HttpMethod.POST,
                requestEntity,
                Void::class.java,
            )
        } returns ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)

        val response = bucketRepository.save(snippet.toString(), snippetContainer, snippetContent, String::class.java)
        assertTrue(response.isPresent)
        assertEquals(response.get(), false)
    }

    @Test
    fun `should success with Optional content while getting snippet content by key`() {
        every {
            restTemplate.exchange(
                "http://asset_service:8080/v1/asset/$snippetContainer/$snippet",
                HttpMethod.GET,
                null,
                String::class.java,
            )
        } returns ResponseEntity(snippetContent, HttpStatus.OK)

        val response = bucketRepository.get(snippet.toString(), snippetContainer, String::class.java)
        assertTrue(response.isPresent)
        assertEquals(response.get(), snippetContent)
    }

    @Test
    fun `should fail with Optional empty because asset service failed with Not_Found status`() {
        every {
            restTemplate.exchange(
                "http://asset_service:8080/v1/asset/$snippetContainer/$snippet",
                HttpMethod.GET,
                null,
                String::class.java,
            )
        } returns ResponseEntity.notFound().build()

        val response = bucketRepository.get(snippet.toString(), snippetContainer, String::class.java)
        assertTrue(response.isEmpty)
    }

    @Test
    fun `should success with Optional true while deleting a snippet content by key`() {
        every {
            restTemplate.exchange(
                "http://asset_service:8080/v1/asset/$snippetContainer/$snippet",
                HttpMethod.DELETE,
                null,
                Void::class.java,
            )
        } returns ResponseEntity(HttpStatus.OK)

        val response = bucketRepository.delete(snippet.toString(), snippetContainer)
        assertTrue(response.isPresent)
        assertEquals(response.get(), true)
    }

    @Test
    fun `should fail with Optional false while deleting a snippet because asset service failed finding`() {
        every {
            restTemplate.exchange(
                "http://asset_service:8080/v1/asset/$snippetContainer/$snippet",
                HttpMethod.DELETE,
                null,
                Void::class.java,
            )
        } returns ResponseEntity(HttpStatus.NOT_FOUND)

        val response = bucketRepository.delete(snippet.toString(), snippetContainer)
        assertTrue(response.isPresent)
        assertEquals(response.get(), false)
    }
}
