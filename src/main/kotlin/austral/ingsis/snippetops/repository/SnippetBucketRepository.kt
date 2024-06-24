package austral.ingsis.snippetops.repository

import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import java.util.Optional

class SnippetBucketRepository(
    val url: String,
    val restTemplate: RestTemplate,
) : BucketRepository {
    private val logger = LoggerFactory.getLogger(SnippetBucketRepository::class.java)

    override fun get(
        key: String,
        container: String,
    ): Optional<Any> {
        val url = "$url/$container/$key"
        return try {
            val response = restTemplate.exchange(url, HttpMethod.GET, null, String::class.java)
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

    override fun save(
        key: String,
        container: String,
        content: String,
    ): Optional<Any> {
        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }
        val requestEntity = HttpEntity(content, headers)
        return try {
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

    override fun getUserRules(
        userId: String,
        container: String,
    ): Optional<Map<String, Any>> {
        val url = "$url/rules/$container/$userId"
        return try {
            val response = restTemplate.exchange(url, HttpMethod.GET, null, Map::class.java)
            if (response.statusCode == HttpStatus.OK) {
                Optional.of(response.body as Map<String, Any>)
            } else {
                Optional.empty()
            }
        } catch (e: Exception) {
            logger.error("Error getting rules from $url", e)
            Optional.empty()
        }
    }

    override fun saveUserRules(
        userId: String,
        container: String,
        rules: Map<String, Any>,
    ): Optional<Boolean> {
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val requestEntity = HttpEntity(rules, headers)
        return try {
            val response =
                restTemplate.exchange(
                    "$url/rules/$container/$userId",
                    HttpMethod.POST,
                    requestEntity,
                    Void::class.java,
                )
            if (response.statusCode == HttpStatus.CREATED) {
                Optional.of(true)
            } else {
                Optional.of(false)
            }
        } catch (e: Exception) {
            logger.error("Error saving rules to $url", e)
            Optional.empty()
        }
    }
}
