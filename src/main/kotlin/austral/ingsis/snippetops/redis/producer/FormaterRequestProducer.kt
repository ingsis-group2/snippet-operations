package austral.ingsis.snippetops.redis.producer

import org.austral.ingsis.redis.RedisStreamProducer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Component
@Profile("!test")
class
FormatterRequestProducer
    @Autowired
    constructor(
        @Value("\${redis.stream.request_formater_key}") streamKey: String,
        redis: RedisTemplate<String, String>,
    ) : RedisStreamProducer(streamKey, redis) {
        suspend fun publishFormatRequest(event: FormaterRequest) {
            println("publishing on format stream: $event")
            emit(event)
        }
    }

data class FormaterRequest(
    val snippetId: Long,
    val snippetContent: String,
    val formatterRules: Map<String, Any>,
)
