package austral.ingsis.snippetops.repository

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

    override fun getUserLintingRules(userId: String): Optional<Map<String, Any>> {
        val url = "$url/rules/lint/$userId"
        return try {
            val response = restTemplate.exchange(url, HttpMethod.GET, null, Map::class.java)
            return when (response.statusCode) {
                HttpStatus.OK -> Optional.of(response.body as Map<String, Any>)
                else -> Optional.empty()
            }
        } catch (e: Exception) {
            Optional.empty()
        }
    }

    override fun saveUserLintingRules(
        userId: String,
        rules: Map<String, Any>,
    ): Boolean {
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val requestEntity = HttpEntity(rules, headers)
        return try {
            val response = restTemplate.exchange("$url/rules/lint/$userId", HttpMethod.POST, requestEntity, Void::class.java)
            response.statusCode == HttpStatus.CREATED
        } catch (ex: HttpClientErrorException) {
            false
        }
    }

    override fun getUserFormattingRules(userId: String): Optional<Map<String, Any>> {
        val url = "$url/rules/format/$userId"
        return try {
            val response = restTemplate.exchange(url, HttpMethod.GET, null, Map::class.java)
            return when (response.statusCode) {
                HttpStatus.OK -> Optional.of(response.body as Map<String, Any>)
                else -> Optional.empty()
            }
        } catch (e: Exception) {
            Optional.empty()
        }
    }

    override fun saveUserFormattingRules(
        userId: String,
        rules: Map<String, Any>,
    ): Boolean {
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val requestEntity = HttpEntity(rules, headers)
        return try {
            val response = restTemplate.exchange("$url/rules/format/$userId", HttpMethod.POST, requestEntity, Void::class.java)
            response.statusCode == HttpStatus.CREATED
        } catch (ex: HttpClientErrorException) {
            false
        }
    }
}
