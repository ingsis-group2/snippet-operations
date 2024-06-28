package austral.ingsis.snippetops.repository

import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.HttpEntity
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import java.util.*

class RulesBucketRepository(
    private val url: String,
    private val restTemplate: RestTemplate,
) : BucketRepository {
    override fun get(key: String, container: String): Optional<Any> {
        val url = buildUrl(key, container)
        return try {
            val response = restTemplate.exchange(url, HttpMethod.GET, null, Map::class.java)
            return when (response.statusCode) {
                HttpStatus.OK -> {
                    println(response.body)
                    return response.body?.let { Optional.of(it) }!!
                }
                else -> Optional.empty()
            }
        } catch (e: Exception) {
            Optional.empty()
        }
    }

    override fun save(key: String, container: String, content: Any): Optional<Any> {
        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }
        val requestEntity = HttpEntity(content, headers)

        // First, delete the existing rules
        delete(key, container)

        return post(container, key, requestEntity)
    }

    override fun delete(key: String, container: String): Optional<Any> {
        return Optional.empty()
    }

    private fun buildUrl(
        key: String,
        container: String,
    ): String {
        return "$url/$container/$key"
    }

    private fun post(
        container: String,
        key: String,
        requestEntity: HttpEntity<*>,
    ): Optional<Any> =
        try {
            val response =
                restTemplate.exchange(
                    "$url/$container/$key",
                    HttpMethod.POST,
                    requestEntity,
                    Void::class.java,
                )
            Optional.of(response.statusCode == HttpStatus.CREATED)
        } catch (ex: HttpClientErrorException) {
            Optional.empty()
        }
}
