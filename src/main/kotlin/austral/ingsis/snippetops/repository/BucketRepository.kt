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

    fun getUserRules(
        userId: String,
        container: String,
    ): Optional<Map<String, Any>>

    fun saveUserRules(
        userId: String,
        container: String,
        rules: Map<String, Any>,
    ): Optional<Boolean>
}
