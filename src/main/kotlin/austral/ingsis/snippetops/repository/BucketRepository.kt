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

    fun getRules(
        key: String,
        container: String,
    ): Optional<Any>

    fun saveRules(
        key: String,
        container: String,
        content: Map<String, Any>,
    ): Optional<Any>

    fun deleteRules(
        key: String,
        container: String,
    ): Optional<Any>
}
