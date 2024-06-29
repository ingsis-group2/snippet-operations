package austral.ingsis.snippetops.config.counter

class NaturalCounter(
    private var value: Int = 0
): Counter {

    override fun increment() {
        this.value += 1
    }

    override fun getValue(): Int {
        return value
    }
}