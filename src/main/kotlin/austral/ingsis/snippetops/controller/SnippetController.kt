package austral.ingsis.snippetops.controller

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@Controller
class SnippetController {

    @GetMapping("/snippets/user")
    @ResponseBody
    fun getAllSnippets(): String {
        return "get my snippets"
    }

    @GetMapping("/snippets/{id}")
    @ResponseBody
    fun getAllSnippets(@PathVariable("id") id: String): String {
        return "get snippet by userId"
    }

    @PostMapping("/snippets")
    @ResponseBody
    fun createSnippet(@RequestBody message: String?): String {
        return "Create a snippet with message"
    }
}