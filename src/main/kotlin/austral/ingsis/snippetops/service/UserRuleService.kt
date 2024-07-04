package austral.ingsis.snippetops.service

import austral.ingsis.snippetops.dto.permissions.SnippetDTO
import austral.ingsis.snippetops.redis.producer.FormaterRequest
import austral.ingsis.snippetops.redis.producer.FormatterRequestProducer
import austral.ingsis.snippetops.redis.producer.LintRequest
import austral.ingsis.snippetops.redis.producer.LintRequestProducer
import austral.ingsis.snippetops.repository.BucketRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class UserRuleService(
    @Autowired val bucketRepository: BucketRepository,
    @Autowired private val snippetService: SnippetService,
    @Autowired private val lintRequestProducer: LintRequestProducer,
    @Autowired private val formaterRequestProducer: FormatterRequestProducer,
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

    private fun defaultLintingRules(): Map<String, Any> =
        mapOf(
            "enablePrintExpressions" to true,
            "caseConvention" to "CAMEL_CASE",
        )

    private fun defaultFormattingRules(): Map<String, Any> =
        mapOf(
            "colonBefore" to true,
            "colonAfter" to true,
            "assignationBefore" to true,
            "assignationAfter" to true,
            "printJump" to 1,
            "ifIndentation" to 1,
        )

    suspend fun publishLintStream(
        userId: String,
        lintingRules: Map<String, Any>,
    ) {
        val snippets = getWriterSnippets(userId)
        snippets.forEach {
            val lintRequest =
                LintRequest(
                    it.id,
                    it.content,
                    lintingRules,
                )
            println("publishing on lint stream: $lintRequest")
            lintRequestProducer.publishLintRequest(lintRequest)
        }
    }

    suspend fun publishFormatStream(
        userId: String,
        formatRules: Map<String, Any>,
    ) {
        val snippets = getWriterSnippets(userId)
        snippets.forEach {
            val formatRequest =
                FormaterRequest(
                    it.id,
                    it.content,
                    formatRules,
                )
            formaterRequestProducer.publishFormatRequest(formatRequest)
        }
    }

    private fun sliceUserId(fullUserId: String): String = fullUserId.substringAfter("|")

    private fun getWriterSnippets(userId: String): List<SnippetDTO> {
        var snippetPageCounter = 0
        var snippets = mutableListOf<SnippetDTO>()
        while (true) {
            val snippetPage = snippetService.getSnippetByWriter(userId, snippetPageCounter)
            if (snippetPage.body == null || snippetPage.body!!.isEmpty()) {
                break
            }
            snippets.addAll(snippetPage.body!!)
            snippetPageCounter++
        }
        return snippets
    }
}
