package austral.ingsis.snippetops.service

import austral.ingsis.snippetops.dto.SnippetDTO
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
    @Autowired private val bucketRepository: BucketRepository,
    private val snippetService: SnippetService,
    private val lintRequestProducer: LintRequestProducer,
    private val formaterRequestProducer: FormatterRequestProducer,
) {
    fun getUserRules(
        userId: String,
        container: String,
    ): ResponseEntity<Map<*, *>> {
        // remove the auth0| prefix from the userId to avoid issues with the bucket repository
        val splicedId = extractAuth0UserId(userId)
        return try {
            val rules = bucketRepository.getRules(splicedId, container)
            if (rules.isPresent) {
                ResponseEntity.ok().body(rules.get() as Map<*, *>)
            } else {
                val defaultRules =
                    if (container == "lint") {
                        defaultLintingRules()
                    } else {
                        defaultFormattingRules()
                    }
                bucketRepository.saveRules(splicedId, container, defaultRules)
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
        val splicedId = extractAuth0UserId(userId)
        return try {
            val result = this.bucketRepository.saveRules(splicedId, container, content)
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
                    it.extension,
                    it.content,
                    lintingRules,
                )
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
                    it.extension,
                    it.content,
                    formatRules,
                )
            formaterRequestProducer.publishFormatRequest(formatRequest)
        }
    }

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

    private fun extractAuth0UserId(fullUserId: String): String = fullUserId.substringAfter("auth0|")
}
