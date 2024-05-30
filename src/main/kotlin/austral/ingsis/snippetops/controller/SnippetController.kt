package austral.ingsis.snippetops.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseBody

@Controller
class SnippetController {
    @GetMapping("/snippets/user")
    @ResponseBody
    fun getAllSnippets(): String {
        return "get my snippets"
    }

    @GetMapping("/snippets/{id}")
    @ResponseBody
    fun getAllSnippets(
        @PathVariable("id") id: String,
    ): String {
        return "get snippet by userId"
    }

    @PostMapping("/snippets")
    @ResponseBody
    fun createSnippet(
        @RequestBody message: String?,
    ): String {
        return "Create a snippet with message"
    }
}
