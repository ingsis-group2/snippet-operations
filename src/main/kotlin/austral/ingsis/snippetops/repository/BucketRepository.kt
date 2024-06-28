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
        content: Any,
    ): Optional<Any>

    fun delete(
        key: String,
        container: String,
    ): Optional<Any>
}
