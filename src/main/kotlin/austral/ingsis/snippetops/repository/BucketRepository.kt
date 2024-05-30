package austral.ingsis.snippetops.repository

interface BucketRepository {
    fun get(id: String, container: String)
    fun save(id: String, container: String)
    fun delete(id: String, container: String)
}