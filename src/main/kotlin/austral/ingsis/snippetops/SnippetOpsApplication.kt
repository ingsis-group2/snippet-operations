package austral.ingsis.snippetops

import io.github.cdimascio.dotenv.Dotenv
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SnippetOpsApplication

fun main(args: Array<String>) {
    runApplication<SnippetOpsApplication>(*args)
}
