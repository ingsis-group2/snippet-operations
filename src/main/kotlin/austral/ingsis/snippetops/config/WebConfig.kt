package austral.ingsis.snippetops.config

import austral.ingsis.snippetops.repository.BucketRepository
import austral.ingsis.snippetops.repository.SnippetBucketRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class WebConfig {
    @Bean
    fun restTemplate(): RestTemplate {
        return RestTemplate()
    }

    @Bean
    fun bucketRepository(): BucketRepository {
        return SnippetBucketRepository()
    }
}
