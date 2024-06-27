package austral.ingsis.snippetops.redis.producer

import org.austral.ingsis.redis.RedisStreamProducer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Component

@Component
class LintRequestProducer
    @Autowired
    constructor(
        @Value("\${stream.keys.linting}") streamKey: String,
        redis: ReactiveRedisTemplate<String, String>,
    ) : RedisStreamProducer(streamKey, redis) {
        suspend fun publishLintRequest(event: LintRequest) {
            println("publishing on stream: $event")
            emit(event)
        }
    }

data class LintRequest(
    val snippetId: Int,
    val version: String,
    val snippetContent: String,
    val lintRules: Map<String, Any>,
)
