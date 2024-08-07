package austral.ingsis.snippetops.redis.producer

import com.example.redisevents.LintRequest
import org.austral.ingsis.redis.RedisStreamProducer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Component
@Profile("!test")
class LintRequestProducer
    @Autowired
    constructor(
        @Value("\${stream.request_linter_key}") streamKey: String,
        redis: RedisTemplate<String, String>,
    ) : RedisStreamProducer(streamKey, redis) {
        suspend fun publishLintRequest(event: LintRequest) {
            println("publishing on lint stream: $event")
            emit(event)
            println("published on lint stream: $event")
        }
    }
