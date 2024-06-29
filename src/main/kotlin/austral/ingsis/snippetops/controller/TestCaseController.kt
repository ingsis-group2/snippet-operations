package austral.ingsis.snippetops.controller

import austral.ingsis.snippetops.dto.operations.CreateTestCase
import austral.ingsis.snippetops.dto.operations.OperationsTestDTO
import austral.ingsis.snippetops.service.TestCaseService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.DeleteMapping

@Controller
@RequestMapping("/testCase")
class TestCaseController(
    @Autowired val testCaseService: TestCaseService
) {

    @PostMapping
    fun createTestCase(
        @RequestBody body: CreateTestCase,
        @AuthenticationPrincipal user: Jwt,
    ): ResponseEntity<OperationsTestDTO> {
        val userId = this.getUserId(user)
        return this.testCaseService.createTestCase(body, userId)
    }

    @GetMapping("/{id}")
    fun getTestCase(
        @PathVariable("id") id: String,
        @AuthenticationPrincipal user: Jwt,
    ): ResponseEntity<Any> {
        val userId = this.getUserId(user)
        return this.testCaseService.getTestCase(id)
    }

    @GetMapping("")
    fun getAllTestCasesByUser(
        @AuthenticationPrincipal user: Jwt,
    ): ResponseEntity<List<Any>> {
        val userId = this.getUserId(user)
        return this.testCaseService.getAllTestsCasesFromUser(userId)
    }

    @DeleteMapping("/{id}")
    fun deleteTestCase(
        @PathVariable("id") id: String,
        @AuthenticationPrincipal user: Jwt,
    ): ResponseEntity<Void> {
        val userId = this.getUserId(user)
        return this.testCaseService.deleteTestCase(id, userId)
    }

    private fun getUserId(jwt: Jwt): String {
        return jwt.claims["sub"].toString()
    }
}