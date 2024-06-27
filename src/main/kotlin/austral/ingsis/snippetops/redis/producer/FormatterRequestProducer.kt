package austral.ingsis.snippetops.redis.producer

import org.austral.ingsis.redis.RedisStreamProducer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Component

@Component
class
FormatterRequestProducer@Autowired
    constructor(
        @Value("\${stream.keys.linting}") streamKey: String,
        redis: ReactiveRedisTemplate<String, String>,
    ) : RedisStreamProducer(streamKey, redis) {
        suspend fun publishFormatRequest(event: FormatterRequest) {
            println("publishing on stream: $event")
            emit(event)
        }
    }

data class FormatterRequest(
    val snippetId: Int,
    val version: String,
    val snippetContent: String,
    val formatterRules: Map<String, Any>,
)
