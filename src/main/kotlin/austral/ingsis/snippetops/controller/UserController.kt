package austral.ingsis.snippetops.controller

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody

@Controller
class UserController {
    @GetMapping("/")
    @ResponseBody
    fun index(): String {
        return "I'm Alive!"
    }

    @GetMapping("/userId")
    @ResponseBody
    fun id(
        @AuthenticationPrincipal user: Jwt,
    ): Any? {
        return user.claims["sub"]
    }

    @GetMapping("/jwt")
    @ResponseBody
    fun jwt(
        @AuthenticationPrincipal jwt: Jwt,
    ): String {
        return jwt.tokenValue
    }
}
