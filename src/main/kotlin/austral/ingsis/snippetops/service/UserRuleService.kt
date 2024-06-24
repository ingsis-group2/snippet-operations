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

    fun getUserLintingRules(userId: String): Map<String, Any> {
        return try {
            val rules = bucketRepository.getUserLintingRules(userId)
            if (rules.isPresent) {
                rules.get()
            } else {
                defaultLintingRules()
            }
        } catch (e: Exception) {
            logger.error("Error fetching linting rules for user $userId", e)
            defaultLintingRules()
        }
    }

    fun saveUserLintingRules(
        userId: String,
        rules: Map<String, Any>,
    ): Boolean {
        return try {
            bucketRepository.saveUserLintingRules(userId, rules)
        } catch (e: Exception) {
            logger.error("Error saving linting rules for user $userId", e)
            false
        }
    }

    fun getUserFormattingRules(userId: String): Map<String, Any> {
        return try {
            val rules = bucketRepository.getUserFormattingRules(userId)
            if (rules.isPresent) {
                rules.get()
            } else {
                defaultFormattingRules()
            }
        } catch (e: Exception) {
            logger.error("Error fetching formatting rules for user $userId", e)
            defaultFormattingRules()
        }
    }

    fun saveUserFormattingRules(
        userId: String,
        rules: Map<String, Any>,
    ): Boolean {
        return try {
            bucketRepository.saveUserFormattingRules(userId, rules)
        } catch (e: Exception) {
            logger.error("Error saving formatting rules for user $userId", e)
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
