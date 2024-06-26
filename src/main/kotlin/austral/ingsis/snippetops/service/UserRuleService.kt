package austral.ingsis.snippetops.service

import austral.ingsis.snippetops.repository.BucketRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class UserRuleService(
    @Autowired val bucketRepository: BucketRepository,
) {
    fun getUserRules(
        userId: String,
        container: String,
    ): ResponseEntity<Map<*, *>> {
        // remove the prefix from the userId to avoid issues with the bucket repository
        val splicedId = sliceUserId(userId)
        return try {
            val rules = bucketRepository.get(splicedId, container, Map::class.java)
            if (rules.isPresent) {
                ResponseEntity.ok().body(rules.get() as Map<*, *>)
            } else {
                val defaultRules =
                    if (container == "lint") {
                        defaultLintingRules()
                    } else {
                        defaultFormattingRules()
                    }
                bucketRepository.save(splicedId, container, defaultRules, Map::class.java)
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
        val splicedId = sliceUserId(userId)
        return try {
            val result = this.bucketRepository.save(splicedId, container, content, Map::class.java)
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
            "ifIndentation" to 1,
        )
    }

    private fun sliceUserId(fullUserId: String): String {
        return fullUserId.substringAfter("|")
    }
}
