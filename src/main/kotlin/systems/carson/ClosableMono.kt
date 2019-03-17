package systems.carson

interface ClosableMono<R> :Mono<R> {
    fun cancel()
}