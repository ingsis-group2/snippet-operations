package austral.ingsis.snippetops.redis.consumer

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
class FormaterRequestConsumer
    @Autowired
    constructor(
        redis: RedisTemplate<String, String>,
        @Value("\${redis.stream.request_linter_result_key}") streamKey: String,
        @Value("\${redis.groups.lint_result}") groupId: String,
    ) : RedisStreamConsumer<FormatResult>(streamKey, groupId, redis) {
        init {
            subscription()
        }

        override fun options(): StreamReceiver.StreamReceiverOptions<String, ObjectRecord<String, FormatResult>> =
            StreamReceiver.StreamReceiverOptions
                .builder()
                .pollTimeout(Duration.ofMillis(10000)) // Set poll rate
                .targetType(FormatResult::class.java) // Set type to de-serialize record
                .build()

        override fun onMessage(record: ObjectRecord<String, FormatResult>) {
        }
    }

data class FormatResult(
    val snippetId: Long,
    val formattedSnippet: String,
)
