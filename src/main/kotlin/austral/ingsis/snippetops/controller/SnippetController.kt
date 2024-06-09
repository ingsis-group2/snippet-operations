package austral.ingsis.snippetops.controller

import austral.ingsis.snippetops.dto.SnippetCreate
import austral.ingsis.snippetops.dto.SnippetLocation
import austral.ingsis.snippetops.service.SnippetService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Controller
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
    @GetMapping("/{id}")
    @ResponseBody
    fun getAllSnippets(
        @PathVariable("id") id: String,
    ): String {
        return "get snippet by userId"
    }

    @PostMapping("")
    fun createSnippet(
        @RequestBody body: SnippetCreate,
    ): ResponseEntity<SnippetLocation> {
        return snippetService.createSnippet(body)
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
