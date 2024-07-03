package austral.ingsis.snippetops.controller

import austral.ingsis.snippetops.service.UserRuleService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
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
    ): ResponseEntity<Map<*, *>> {
        val userId = user.claims["sub"].toString()
        val rules = userRuleService.getUserRules(userId, "lint")
        return rules
    }

    @PostMapping("/lint")
    suspend fun saveUserRules(
        @AuthenticationPrincipal user: Jwt,
        @RequestBody rules: Map<String, Any>,
    ): ResponseEntity<Boolean> {
        val userId = user.claims["sub"].toString()
        val response = userRuleService.saveUserRules(userId, rules, "lint")
        if (response.statusCode.is2xxSuccessful) {
            userRuleService.publishLintStream(userId, rules)
        }
        return response
    }

    @GetMapping("/format")
    fun getUserFormattingRules(
        @AuthenticationPrincipal user: Jwt,
    ): ResponseEntity<Map<*, *>> {
        val userId = user.claims["sub"].toString()
        val rules = userRuleService.getUserRules(userId, "format")
        return rules
    }

    @PostMapping("/format")
    suspend fun saveUserFormattingRules(
        @AuthenticationPrincipal user: Jwt,
        @RequestBody rules: Map<String, Any>,
    ): ResponseEntity<Boolean> {
        val userId = user.claims["sub"].toString()
        val response = userRuleService.saveUserRules(userId, rules, "format")
        if (response.statusCode.is2xxSuccessful) {
            userRuleService.publishFormatStream(userId, rules)
        }
        return response
    }
}
