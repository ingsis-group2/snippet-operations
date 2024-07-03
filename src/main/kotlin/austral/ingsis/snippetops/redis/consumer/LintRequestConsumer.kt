package austral.ingsis.snippetops.redis.consumer

import org.austral.ingsis.redis.RedisStreamConsumer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.stream.StreamReceiver
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class LinterConsumer
    @Autowired
    constructor(
        redis: ReactiveRedisTemplate<String, String>,
        @Value("\${redis.stream.request_linter_result_key}") streamKey: String,
        @Value("\${redis.groups.lint_result}") groupId: String,
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
        }
    }

data class LintResultEvent(
    val snippetId: Long,
    val reportList: List<String>,
    val errorList: List<String>,
)