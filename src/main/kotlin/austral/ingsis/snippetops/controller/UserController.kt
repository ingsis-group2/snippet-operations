package austral.ingsis.snippetops.controller

import austral.ingsis.snippetops.dto.User
import austral.ingsis.snippetops.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/user")
class UserController(
    @Autowired val userService: UserService,
) {
    @GetMapping
    fun getAllUsers(
        @AuthenticationPrincipal user: Jwt,
    ): ResponseEntity<List<User>> {
        return ResponseEntity.ok(userService.getAllUsers())
    }
}
