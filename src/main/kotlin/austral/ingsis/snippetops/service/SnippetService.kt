package austral.ingsis.snippetops.service

import austral.ingsis.snippetops.dto.SnippetLocation
import austral.ingsis.snippetops.repository.BucketRepository
import austral.ingsis.snippetops.repository.SnippetBucketRepository
import com.nimbusds.jose.shaded.gson.Gson
import com.nimbusds.jose.shaded.gson.reflect.TypeToken
import org.springframework.stereotype.Service
import java.net.HttpURLConnection
import java.net.URL

@Service
class SnippetService(
    val url: String = "http://localhost:8081/permission",
    val bucketRepository: BucketRepository = SnippetBucketRepository(),
) {
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
