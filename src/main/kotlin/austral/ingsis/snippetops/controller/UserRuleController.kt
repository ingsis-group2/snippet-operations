package austral.ingsis.snippetops.controller

import austral.ingsis.snippetops.redis.producer.LintRequestProducer
import austral.ingsis.snippetops.service.UserRuleService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/rules")
@CrossOrigin("*")
class UserRuleController(
    @Autowired private val userRuleService: UserRuleService,
    @Autowired private val lintProducer: LintRequestProducer,
) {
    @GetMapping("/lint")
    fun getUserLintingRules(
        @AuthenticationPrincipal user: Jwt,
    ): ResponseEntity<Map<String, Any>> {
        val userId = user.claims["sub"].toString()
        val rules = userRuleService.getUserLintingRules(userId)
        return ok(rules)
    }

    @PostMapping("/lint")
    fun saveUserRules(
        @AuthenticationPrincipal user: Jwt,
        @RequestBody rules: Map<String, Any>,
    ): ResponseEntity<Void> {
        val userId = user.claims["sub"].toString()
        return if (userRuleService.saveUserLintingRules(userId, rules)) {
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
        val rules = userRuleService.getUserFormattingRules(userId)
        return ok(rules)
    }

    @PostMapping("/format")
    fun saveUserFormattingRules(
        @AuthenticationPrincipal user: Jwt,
        @RequestBody rules: Map<String, Any>,
    ): ResponseEntity<Void> {
        val userId = user.claims["sub"].toString()
        return if (userRuleService.saveUserFormattingRules(userId, rules)) {
            ResponseEntity.status(201).build()
        } else {
            ResponseEntity.status(500).build()
        }
    }
}
