package austral.ingsis.snippetops.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@Controller
class SnippetController {

    @GetMapping("/snippets/user")
    @ResponseBody
    fun getAllMessages(): String {
        return "get my snippets"
    }

    @GetMapping("/snippets/{id}")
    @ResponseBody
    fun getSingleMessage(@PathVariable id: String): String {
        return "get snippet by id"
    }

    @PostMapping("/snippets")
    @ResponseBody
    fun createMessage(@RequestBody message: String?): String {
        return "Create a snippet with message"
    }
}