package systems.carson

/**
 * Transforms a value into a [Mono] that contains that value
 */
fun <T> T.toMono():Mono<T>{
    return Mono.just(this)
}

/**
 * Returns a [Mono] which zip this value and the other [Mono] using the processor
 */
fun <R,T,E> T.zipToMono(mono :Mono<R>, processor :(T,R) -> E):Mono<E>{
    return Mono.zip(Mono.just(this),mono,processor)
}