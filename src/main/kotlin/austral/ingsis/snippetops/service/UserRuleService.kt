package austral.ingsis.snippetops.service

import austral.ingsis.snippetops.repository.BucketRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.Optional

@Service
class UserRuleService(
    @Autowired private val bucketRepository: BucketRepository,
) {
    fun getUserLintingRules(userId: String): Optional<Map<String, Any>> {
        val rules = bucketRepository.getUserLintingRules(userId)
        return if (rules.isPresent) {
            rules
        } else {
            Optional.of(
                mapOf(
                    "enablePrintExpressions" to false,
                    "caseConvention" to "CAMEL_CASE",
                ),
            )
        }
    }

    fun saveUserLintingRules(
        userId: String,
        rules: Map<String, Any>,
    ): Boolean {
        return bucketRepository.saveUserLintingRules(userId, rules)
    }

    fun getUserFormattingRules(userId: String): Optional<Map<String, Any>> {
        val rules = bucketRepository.getUserFormattingRules(userId)
        return if (rules.isPresent) {
            rules
        } else {
            Optional.of(
                mapOf(
                    "colonBefore" to true,
                    "colonAfter" to true,
                    "assignationBefore" to true,
                    "assignationAfter" to true,
                    "printJump" to 1,
                ),
            )
        }
    }

    fun saveUserFormattingRules(
        userId: String,
        rules: Map<String, Any>,
    ): Boolean {
        return bucketRepository.saveUserFormattingRules(userId, rules)
    }
}
