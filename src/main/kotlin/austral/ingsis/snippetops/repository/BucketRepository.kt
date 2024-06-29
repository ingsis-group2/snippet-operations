package austral.ingsis.snippetops.repository

import java.util.Optional

interface BucketRepository {
    fun <T> get(
        key: String,
        container: String,
        expectedType: Class<T>,
    ): Optional<Any>

    fun <T> save(
        key: String,
        container: String,
        content: Any,
        type: Class<T>,
    ): Optional<Any>

    fun delete(
        key: String,
        container: String,
    ): Optional<Any>
}
