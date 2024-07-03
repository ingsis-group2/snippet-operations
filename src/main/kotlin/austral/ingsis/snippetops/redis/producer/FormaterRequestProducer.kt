package austral.ingsis.snippetops.redis.producer

import kotlinx.coroutines.reactor.awaitSingle
import org.austral.ingsis.redis.RedisStreamProducer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Component

@Component
class
FormatterRequestProducer@Autowired
    constructor(
        @Value("\${redis.stream.request_formater_key}") streamKey: String,
        redis: ReactiveRedisTemplate<String, String>,
    ) : RedisStreamProducer(streamKey, redis) {
        suspend fun publishFormatRequest(event: FormaterRequest) {
            println("publishing on format stream: $event")
            emit(event).awaitSingle()
        }
    }

data class FormaterRequest(
    val snippetId: Long,
    val snippetContent: String,
    val formatterRules: Map<String, Any>,
)
