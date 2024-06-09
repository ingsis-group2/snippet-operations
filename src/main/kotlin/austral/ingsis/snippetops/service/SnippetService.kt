package austral.ingsis.snippetops.service

import austral.ingsis.snippetops.dto.SnippetCreate
import austral.ingsis.snippetops.dto.SnippetLocation
import austral.ingsis.snippetops.repository.BucketRepository
import com.nimbusds.jose.shaded.gson.Gson
import com.nimbusds.jose.shaded.gson.reflect.TypeToken
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import java.net.HttpURLConnection
import java.net.URL

@Service
class SnippetService(
    @Value("\${spring.services.snippet.permissions}") val url: String,
    @Autowired val bucketRepository: BucketRepository,
    @Autowired var restTemplate: RestTemplate,
) {
    fun createSnippet(body: SnippetCreate): ResponseEntity<SnippetLocation> {
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }
        val requestEntity = HttpEntity(body, headers)
        return try {
            val response = restTemplate.exchange("$url/snippet", HttpMethod.POST, requestEntity, SnippetLocation::class.java)
            response
        } catch (ex: HttpClientErrorException) {
            // Manejo de errores
            println("Error response: ${ex.responseBodyAsString}")
            ResponseEntity.status(ex.statusCode).build()
        }
    }
    fun getSharedAndWrittenByUserId(userId: String): List<SnippetLocation> {
        val url = URL("$url/snippet/all/$userId")
        return getSnippets(url)
    }

    fun getSharedSnippets(userId: String): List<SnippetLocation> {
        val url = URL("$url/snippet/$userId")
        return getSnippets(url)
    }

    fun getWrittenSNippets(userId: String): List<SnippetLocation> {
        val url = URL("$url/snippet/$userId")
        return getSnippets(url)
    }

    private fun getSnippets(url: URL): List<SnippetLocation> {
        val con: HttpURLConnection = url.openConnection() as HttpURLConnection
        con.requestMethod = "GET"

        return try {
            val responseCode = con.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val responseBody = con.inputStream.bufferedReader().use { it.readText() }
                val gson = Gson()
                val snippetLocationType = object : TypeToken<List<SnippetLocation>>() {}.type
                gson.fromJson(responseBody, snippetLocationType)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        } finally {
            con.disconnect()
        }
    }
}
