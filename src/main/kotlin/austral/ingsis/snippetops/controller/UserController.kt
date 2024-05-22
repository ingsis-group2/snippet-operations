package austral.ingsis.snippetperms.controller


import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*


@Controller
class UserController {

    @GetMapping("/")
    @ResponseBody
    fun index(): String {
        return "I'm Alive!"
    }

    @GetMapping("/userId")
    @ResponseBody
    fun id(@AuthenticationPrincipal user: Jwt): Any? {
        return user.claims["sub"]
    }

    @GetMapping("/jwt")
    @ResponseBody
    fun jwt(@AuthenticationPrincipal jwt: Jwt): String {
        return jwt.tokenValue
    }
}
