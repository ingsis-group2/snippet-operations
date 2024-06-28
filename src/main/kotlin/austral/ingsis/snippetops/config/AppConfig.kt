package austral.ingsis.snippetops.config

import austral.ingsis.snippetops.repository.BucketRepository
import austral.ingsis.snippetops.repository.RulesBucketRepository
import austral.ingsis.snippetops.repository.SnippetBucketRepository
import austral.ingsis.snippetops.repository.TestCaseBucketRepository
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.web.client.RestTemplate

@Configuration
class AppConfig {

    @Bean
    fun restTemplate(): RestTemplate {
        return RestTemplateBuilder()
            .additionalInterceptors(
                ClientHttpRequestInterceptor { request, body, execution ->
                    println("Request URI: ${request.uri}")
                    println("Request Body: ${String(body)}")
                    execution.execute(request, body)
                },
            )
            .build()
    }

    @Bean
    @Qualifier("snippetBucketRepository")
    fun snippetBucketRepository(
        restTemplate: RestTemplate,
        @Value("\${}") url: String
    ): BucketRepository {
        return SnippetBucketRepository(url, restTemplate)
    }

    @Bean
    @Qualifier("testCaseBucketRepository")
    fun testCaseBucketRepository(
        restTemplate: RestTemplate,
        @Value("\${spring.services.testcase.permissions}") url: String
    ): BucketRepository {
        return TestCaseBucketRepository(url, restTemplate)
    }

    @Bean
    @Qualifier("rulesBucketRepository")
    fun rulesBucketRepository(
        restTemplate: RestTemplate,
        @Value("\${spring.services.snippet.bucket}") url: String,
    ): BucketRepository {
        return RulesBucketRepository(url, restTemplate)
    }
}
