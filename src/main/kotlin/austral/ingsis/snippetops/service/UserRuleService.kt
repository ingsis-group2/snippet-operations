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
        return try {
            val rules = bucketRepository.getRules(userId, container)
            if (rules.isPresent) {
                ResponseEntity.ok().body(rules.get() as Map<*, *>)
            } else {
                val defaultRules =
                    if (container == "lint") {
                        defaultLintingRules()
                    } else {
                        defaultFormattingRules()
                    }
                bucketRepository.saveRules(userId, container, defaultRules)
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
        return try {
            val result = this.bucketRepository.saveRules(userId, container, content)
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
}
