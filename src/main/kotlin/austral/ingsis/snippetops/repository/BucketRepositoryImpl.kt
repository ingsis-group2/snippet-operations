package austral.ingsis.snippetops.repository

import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import java.util.Optional

class BucketRepositoryImpl(
    private val url: String,
    private val restTemplate: RestTemplate,
) : BucketRepository {
    override fun <T> get(
        key: String,
        container: String,
        expectedType: Class<T>,
    ): Optional<Any> {
        val url = buildUrl(key, container)
        return try {
            val response = restTemplate.exchange(url, HttpMethod.GET, null, expectedType)
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

    override fun <T> save(
        key: String,
        container: String,
        content: Any,
        type: Class<T>,
    ): Optional<Any> {
        val asset = this.get(key, container, type)
        if (asset.isPresent) { // if is not present, then is
            this.delete(key, container)
        }
        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }
        val requestEntity = HttpEntity(content, headers)

        return post(container, key, requestEntity)
    }

    override fun delete(
        key: String,
        container: String,
    ): Optional<Any> {
        val url = "$url/$container/$key"
        return try {
            val response = restTemplate.exchange(url, HttpMethod.DELETE, null, Void::class.java)
            Optional.of(response.statusCode == HttpStatus.OK)
        } catch (e: Exception) {
            Optional.empty()
        }
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

    private fun buildUrl(
        key: String,
        container: String,
    ): String {
        return "$url/$container/$key"
    }
}
