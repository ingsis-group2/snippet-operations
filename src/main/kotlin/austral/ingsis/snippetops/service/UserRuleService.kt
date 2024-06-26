package austral.ingsis.snippetops.service

import austral.ingsis.snippetops.repository.BucketRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class UserRuleService(
    @Autowired private val bucketRepository: BucketRepository,
) {
    fun getUserRules(
        userId: String,
        container: String,
    ): ResponseEntity<Map<*, *>> {
        // remove the auth0| prefix from the userId to avoid issues with the bucket repository
        val splicedId = extractAuth0UserId(userId)
        return try {
            val rules = bucketRepository.getRules(splicedId, container)
            if (rules.isPresent) {
                ResponseEntity.ok().body(rules.get() as Map<*, *>)
            } else {
                val defaultRules =
                    if (container == "lint") {
                        defaultLintingRules()
                    } else {
                        defaultFormattingRules()
                    }
                bucketRepository.saveRules(splicedId, container, defaultRules)
                ResponseEntity.ok().body(defaultRules)
            }
        } catch (e: Exception) {
            ResponseEntity.status(500).build()
        }
    }

    fun saveUserRules(
        userId: String,
        content: Map<String, Any>,
        container: String,
    ): ResponseEntity<Boolean> {
        val splicedId = extractAuth0UserId(userId)
        return try {
            val result = this.bucketRepository.saveRules(splicedId, container, content)
            if (result.isPresent) {
                if (result.get() == true) {
                    ResponseEntity.status(201).build()
                } else {
                    ResponseEntity.status(500).build()
                }
            } else {
                ResponseEntity.status(500).build()
            }
        } catch (e: Exception) {
            ResponseEntity.status(500).build()
        }
    }

    private fun defaultLintingRules(): Map<String, Any> {
        return mapOf(
            "enablePrintExpressions" to true,
            "caseConvention" to "CAMEL_CASE",
        )
    }

    private fun defaultFormattingRules(): Map<String, Any> {
        return mapOf(
            "colonBefore" to true,
            "colonAfter" to true,
            "assignationBefore" to true,
            "assignationAfter" to true,
            "printJump" to 1,
        )
    }

    private fun extractAuth0UserId(fullUserId: String): String {
        return fullUserId.substringAfter("auth0|")
    }
}
