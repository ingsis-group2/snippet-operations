package austral.ingsis.snippetops.config.counter

interface Counter {
    fun increment()
    fun getValue(): Int
}