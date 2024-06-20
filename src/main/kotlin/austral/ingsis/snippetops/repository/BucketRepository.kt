package austral.ingsis.snippetops.repository

import java.util.Optional

interface BucketRepository {
    fun get(
        key: String,
        container: String,
    ): Optional<Any>

    fun save(
        key: String,
        container: String,
        content: String,
    ): Optional<Any>

    fun delete(
        key: String,
        container: String,
    ): Optional<Any>

    fun getUserLintingRules(userId: String): Optional<Map<String, Any>>

    fun saveUserLintingRules(
        userId: String,
        rules: Map<String, Any>,
    ): Boolean

    fun getUserFormattingRules(userId: String): Optional<Map<String, Any>>

    fun saveUserFormattingRules(
        userId: String,
        rules: Map<String, Any>,
    ): Boolean
}
