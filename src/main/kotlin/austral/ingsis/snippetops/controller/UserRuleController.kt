package austral.ingsis.snippetops.controller

import austral.ingsis.snippetops.service.UserRuleService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/rules")
class UserRuleController(
    @Autowired private val userRuleService: UserRuleService,
) {
    @GetMapping("/lint")
    fun getUserLintingRules(
        @AuthenticationPrincipal user: Jwt,
    ): ResponseEntity<Map<String, Any>> {
        val userId = user.claims["sub"].toString()
        val rules = userRuleService.getUserRules(userId, "lint")
        return ok(rules)
    }

    @PostMapping("/lint")
    fun saveUserRules(
        @AuthenticationPrincipal user: Jwt,
        @RequestBody rules: Map<String, Any>,
    ): ResponseEntity<Void> {
        val userId = user.claims["sub"].toString()
        return if (userRuleService.saveUserRules(userId, rules, "lint")) {
            ResponseEntity.status(201).build()
        } else {
            ResponseEntity.status(500).build()
        }
    }

    @GetMapping("/format")
    fun getUserFormattingRules(
        @AuthenticationPrincipal user: Jwt,
    ): ResponseEntity<Map<String, Any>> {
        val userId = user.claims["sub"].toString()
        val rules = userRuleService.getUserRules(userId, "format")
        return ok(rules)
    }

    @PostMapping("/format")
    fun saveUserFormattingRules(
        @AuthenticationPrincipal user: Jwt,
        @RequestBody rules: Map<String, Any>,
    ): ResponseEntity<Void> {
        val userId = user.claims["sub"].toString()
        return if (userRuleService.saveUserRules(userId, rules, "format")) {
            ResponseEntity.status(201).build()
        } else {
            ResponseEntity.status(500).build()
        }
    }
}
