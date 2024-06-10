package austral.ingsis.snippetops.controller

import austral.ingsis.snippetops.dto.SnippetCreate
import austral.ingsis.snippetops.service.SnippetService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
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
    ): ResponseEntity<Boolean> {
        return snippetService.createSnippet(body)
    }

    @GetMapping("/{id}")
    fun getSnippetById(
        @RequestParam(value = "id", required = true) id: String,
    ): ResponseEntity<Any> {
        return snippetService.getSnippet(id)
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
