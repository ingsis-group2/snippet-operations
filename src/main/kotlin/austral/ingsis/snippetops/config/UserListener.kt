package austral.ingsis.snippetops.config

import austral.ingsis.snippetops.dto.UserDTO
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Controller
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

@Controller
class UserListener {
    fun listenUser(jwt: Jwt) {
        when {
            !this.exists(jwt) -> this.create(jwt)
        }
    }

    private fun exists(jwt: Jwt): Boolean {
        val userId = jwt.claims["sub"]
        val url = URL("http://localhost:8081/permission/user/$userId")
        val con: HttpURLConnection = url.openConnection() as HttpURLConnection
        con.requestMethod = "GET"

        return try {
            val responseCode = con.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val responseBody = con.inputStream.bufferedReader().use { it.readText() }
                responseBody.toBoolean()
            } else {
                false
            }
        } catch (e: Exception) {
            false
        } finally {
            con.disconnect()
        }
    }

    private fun create(jwt: Jwt): Boolean {
        val url = URL("http://localhost:8081/permission/user")
        val con: HttpURLConnection = url.openConnection() as HttpURLConnection
        con.requestMethod = "POST"
        con.doOutput = true
        con.setRequestProperty("Content-Type", "application/json")

        val userId = jwt.claims["sub"]
        val objectMapper = jacksonObjectMapper()
        val userJson = objectMapper.writeValueAsString(UserDTO(userId.toString()))

        return try {
            val outputWriter = OutputStreamWriter(con.outputStream)
            outputWriter.write(userJson)
            outputWriter.flush()
            outputWriter.close()

            val responseCode = con.responseCode
            responseCode == HttpURLConnection.HTTP_OK
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            con.disconnect()
        }
    }
}
