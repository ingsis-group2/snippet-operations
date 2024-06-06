package austral.ingsis.snippetops.repository

interface BucketRepository {
    fun get(
        key: String,
        container: String,
    ): Any?

    fun save(
        key: String,
        container: String,
    ): Any?

    fun delete(
        key: String,
        container: String,
    ): Any?
}
