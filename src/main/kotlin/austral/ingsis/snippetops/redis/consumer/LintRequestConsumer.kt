package austral.ingsis.snippetops.redis.consumer

import austral.ingsis.snippetops.dto.permissions.UpdateLintStatusDTO
import austral.ingsis.snippetops.service.SnippetService
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
        @Value("\${spring.data.redis.stream.request_linter_result_key}") streamKey: String,
        @Value("\${spring.data.redis.groups.lint_result}") groupId: String,
        private val snippetService: SnippetService,
    ) : RedisStreamConsumer<LintResultEvent>(streamKey, groupId, redis) {
        init {
            subscription()
        }

        override fun options(): StreamReceiver.StreamReceiverOptions<String, ObjectRecord<String, LintResultEvent>> =
            StreamReceiver.StreamReceiverOptions
                .builder()
                .pollTimeout(Duration.ofMillis(10000)) // Set poll rate
                .targetType(LintResultEvent::class.java) // Set type to de-serialize record
                .build()

        override fun onMessage(record: ObjectRecord<String, LintResultEvent>) {
            println("message received on linter result stream: ${record.value}")
            snippetService.updateSnippetLintStatus(
                UpdateLintStatusDTO(record.value.snippetId, record.value.reportList, record.value.errorList),
            )
        }
    }

data class LintResultEvent(
    val snippetId: Long,
    val reportList: List<String>,
    val errorList: List<String>,
)
