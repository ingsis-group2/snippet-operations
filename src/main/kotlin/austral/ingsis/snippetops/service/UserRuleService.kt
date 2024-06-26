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
        try {
            val rules = this.bucketRepository.getRules(userId, container)
            return when {
                rules.isPresent -> ResponseEntity.ok().body(rules.get() as Map<*, *>)
                else ->
                    if (container == "lint") {
                        try {
                            // save default rules and return them
                            this.bucketRepository.saveRules(userId, container, defaultLintingRules())
                            ResponseEntity.ok().body(defaultLintingRules())
                        } catch (e: Exception) {
                            ResponseEntity.status(500).build()
                        }
                    } else {
                        try {
                            // save default rules and return them
                            this.bucketRepository.saveRules(userId, container, defaultFormattingRules())
                            ResponseEntity.ok().body(defaultFormattingRules())
                        } catch (e: Exception) {
                            ResponseEntity.status(500).build()
                        }
                    }
            }
        } catch (e: Exception) {
            return ResponseEntity.status(500).build()
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
