package austral.ingsis.snippetops.controller

import austral.ingsis.snippetops.dto.SnippetCreate
import austral.ingsis.snippetops.service.SnippetService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.client.RestTemplate

@Controller
@RequestMapping("/snippet")
class SnippetController(
    @Autowired val snippetService: SnippetService,
    @Autowired val restTemplate: RestTemplate,
) {
    @PostMapping("")
    fun createSnippet(
        @RequestBody body: SnippetCreate,
        @AuthenticationPrincipal user: Jwt,
    ): ResponseEntity<Boolean> {
        val userId = user.claims["sub"]
        return snippetService.createSnippet(body, userId.toString())
    }

    @GetMapping("/{id}")
    fun getSnippetById(
        @PathVariable(value = "id", required = true) id: String,
    ): ResponseEntity<Any> {
        return snippetService.getSnippet(id)
    }

    @DeleteMapping("/{id}")
    fun deleteById(
        @PathVariable("id") id: Long,
    ): ResponseEntity<Boolean> {
        return this.snippetService.deleteSnippet(id)
    }

    @GetMapping("/greet")
    @ResponseBody
    fun greet(): String {
        return "hello stranger"
    }

    @GetMapping("/greet/permissions")
    fun greetPermissions(): ResponseEntity<String> {
        val url = "http://snippet-permissions:8080/snippet/greetBack"
        val response = restTemplate.exchange(url, HttpMethod.GET, null, String::class.java)
        return ResponseEntity(response.body, response.statusCode)
    }
}
