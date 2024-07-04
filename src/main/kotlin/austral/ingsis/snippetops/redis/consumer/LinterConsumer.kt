package austral.ingsis.snippetops.redis.consumer

import austral.ingsis.snippetops.dto.permissions.UpdateLintStatusDTO
import austral.ingsis.snippetops.service.SnippetService
import com.example.redisevents.LintResult
import org.austral.ingsis.redis.RedisStreamConsumer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.stream.StreamReceiver
import org.springframework.stereotype.Component
import java.time.Duration

@Component
@Profile("!test")
class LinterConsumer
    @Autowired
    constructor(
        redis: RedisTemplate<String, String>,
        @Value("\${stream.request_linter_result_key}") streamKey: String,
        @Value("\${groups.lint_result}") groupId: String,
        private val snippetService: SnippetService,
    ) : RedisStreamConsumer<LintResult>(streamKey, groupId, redis) {
        init {
            subscription()
        }

        override fun options(): StreamReceiver.StreamReceiverOptions<String, ObjectRecord<String, LintResult>> =
            StreamReceiver.StreamReceiverOptions
                .builder()
                .pollTimeout(Duration.ofMillis(10000)) // Set poll rate
                .targetType(LintResult::class.java) // Set type to de-serialize record
                .build()

        override fun onMessage(record: ObjectRecord<String, LintResult>) {
            println("------------------------------------------------------")
            println("message received on linter result stream: ${record.value}")
            println("snippet id: ${record.value.snippetId}")
            println("report list: ${record.value.reportList}")
            println("error list: ${record.value.errorList}")
            val response =
                snippetService.updateSnippetLintStatus(
                    UpdateLintStatusDTO(record.value.snippetId, record.value.reportList, record.value.errorList),
                )
            if (response.statusCode.is2xxSuccessful) {
                println("Snippet lint status updated successfully")
                println("------------------------------------------------------")
            } else {
                println("Failed to update snippet lint status")
                println("------------------------------------------------------")
            }
        }
    }
