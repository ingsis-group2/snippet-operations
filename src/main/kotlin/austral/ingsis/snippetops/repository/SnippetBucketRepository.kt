package austral.ingsis.snippetops.repository

import austral.ingsis.snippetops.dto.SnippetDTO
import com.nimbusds.jose.shaded.gson.Gson
import java.net.HttpURLConnection
import java.net.URL

class SnippetBucketRepository(
    val url: String = "http://localhost:8080/",
) : BucketRepository {
    override fun get(
        key: String,
        container: String,
    ): SnippetDTO? {
        val url = URL("$url/v1/asset/{$container}/{$key}")
        val con: HttpURLConnection = url.openConnection() as HttpURLConnection
        con.requestMethod = "GET"

        return try {
            val responseCode = con.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val responseBody = con.inputStream.bufferedReader().use { it.readText() }
                val gson = Gson()
                gson.fromJson(responseBody, SnippetDTO::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            con.disconnect()
        }
    }

    override fun save(
        id: String,
        container: String,
    ) {
        TODO("Not yet implemented")
    }

    override fun delete(
        id: String,
        container: String,
    ) {
        TODO("Not yet implemented")
    }
}
