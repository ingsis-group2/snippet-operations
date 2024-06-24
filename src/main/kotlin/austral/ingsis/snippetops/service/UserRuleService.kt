package austral.ingsis.snippetops.service

import austral.ingsis.snippetops.repository.BucketRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class UserRuleService(
    @Autowired private val bucketRepository: BucketRepository,
) {
    private val logger = LoggerFactory.getLogger(UserRuleService::class.java)

    fun getUserRules(
        userId: String,
        container: String,
    ): Map<String, Any> {
        return try {
            val rules = bucketRepository.getUserRules(userId, container)
            if (rules.isPresent) {
                rules.get()
            } else {
                when (container) {
                    "lint" -> defaultLintingRules()
                    "format" -> defaultFormattingRules()
                    else -> emptyMap()
                }
            }
        } catch (e: Exception) {
            logger.error("Error fetching rules for user $userId", e)
            emptyMap()
        }
    }

    fun saveUserRules(
        userId: String,
        rules: Map<String, Any>,
        container: String,
    ): Boolean {
        return try {
            val result = bucketRepository.saveUserRules(userId, container, rules)
            result.orElse(false)
        } catch (e: Exception) {
            logger.error("Error saving rules for user $userId", e)
            false
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
