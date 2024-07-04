package austral.ingsis.snippetops.redis.producer

import com.example.redisevents.FormaterRequest
import org.austral.ingsis.redis.RedisStreamProducer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Component
@Profile("!test")
class
FormaterRequestProducer
    @Autowired
    constructor(
        @Value("\${stream.request_formater_key}") streamKey: String,
        redis: RedisTemplate<String, String>,
    ) : RedisStreamProducer(streamKey, redis) {
        suspend fun publishFormatRequest(event: FormaterRequest) {
            println("publishing on format stream: $event")
            emit(event)
            println("published on format stream: $event")
        }
    }
