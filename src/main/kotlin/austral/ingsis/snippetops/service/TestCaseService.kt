package austral.ingsis.snippetops.service

import austral.ingsis.snippetops.dto.operations.CreateTestCase
import austral.ingsis.snippetops.dto.operations.OperationsTestDTO
import austral.ingsis.snippetops.repository.BucketRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class TestCaseService(
    @Autowired val bucketRepository: BucketRepository,
) {
    fun createTestCase(
        body: CreateTestCase,
        userId: String,
    ): ResponseEntity<OperationsTestDTO> {
        val testCaseId = UUID.randomUUID().toString()
        val testCase =
            OperationsTestDTO(
                id = testCaseId,
                name = body.name,
                snippetId = body.snippetId,
                version = body.version,
                inputs = body.inputs,
                envs = body.envs,
                output = body.output,
            )
        bucketRepository.save(testCaseId, "test", testCase, OperationsTestDTO::class.java)
        val isSaved = this.saveId(userId, testCaseId)
        return when {
            isSaved -> ResponseEntity(testCase, HttpStatus.CREATED)
            else -> ResponseEntity.badRequest().build()
        }
    }

    fun getTestCase(testCaseId: String): ResponseEntity<Any> {
        val testCase = this.bucketRepository.get(testCaseId, "test", OperationsTestDTO::class.java)
        return when {
            testCase.isPresent -> ResponseEntity.ok(testCase.get())
            else -> ResponseEntity.notFound().build()
        }
    }

    fun getAllTestsCasesFromUser(userId: String): ResponseEntity<List<Any>> {
        val key = userId.substring(6, userId.length)
        val testCasesIds = bucketRepository.get(key, "testCaseId", List::class.java)
        return when {
            testCasesIds.isEmpty() -> ResponseEntity.notFound().build()
            else -> {
                val ids = testCasesIds.get() as List<*>
                val response = mutableListOf<Any>()
                ids.forEach { id -> this.getTestCase(id.toString()) }
                return ResponseEntity.ok(response.toList())
            }
        }
    }

    fun getTestCaseFromSnippet(
        snippetId: Long,
        userId: String,
    ): ResponseEntity<List<Any>> {
        val testCases = getAllTestsCasesFromUser(userId)
        return when (testCases.statusCode) {
            HttpStatus.OK -> {
                val response = mutableListOf<Any>()
                val body = testCases.body as List<*>
                for (testCase in body) {
                    testCase as OperationsTestDTO
                    if (testCase.snippetId == snippetId) {
                        response.add(response)
                    }
                }
                ResponseEntity.ok(response.toList())
            }
            else -> ResponseEntity.notFound().build()
        }
    }

    fun deleteTestCase(
        testCaseId: String,
        userId: String,
    ): ResponseEntity<Void> {
        val isIdDeleted = this.deleteId(userId, testCaseId)
        val response = this.bucketRepository.delete(testCaseId, "test")
        return when {
            response.isEmpty || !isIdDeleted -> ResponseEntity.notFound().build()
            else -> ResponseEntity.ok().build()
        }
    }

    private fun saveId(
        userId: String,
        testCaseId: String,
    ): Boolean {
        val key = userId.substring(6, userId.length)
        val data = bucketRepository.get(key, "testCaseId", List::class.java)
        if (data.isEmpty()) {
            val newData = listOf(testCaseId)
            return bucketRepository.save(key, "testCaseId", newData, List::class.java).isPresent
        } else {
            val newData = mutableListOf<String>()
            val unboxedData = data.get() as List<*>
            unboxedData.forEach { id -> newData.add(id.toString()) }
            newData.add(testCaseId)
            return bucketRepository.save(key, "testCaseId", newData.toList(), List::class.java).isPresent
        }
    }

    private fun deleteId(
        userId: String,
        testCaseId: String,
    ): Boolean {
        val key = userId.substring(6, userId.length)
        val data = bucketRepository.get(key, "testCaseId", List::class.java)
        if (data.isPresent) {
            val lst = data.get() as List<*>
            val newData = mutableListOf<String>()
            for (id in lst) {
                if (id.toString() != testCaseId) {
                    newData.add(id.toString())
                }
            }
            return bucketRepository.save(key, "testCaseId", newData.toList(), List::class.java).isPresent
        }
        return false
    }
}
