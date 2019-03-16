package systems.carson

import systems.carson.impl.CallableMono
import systems.carson.impl.ValueMono

/**
 * Returns a mono for the value. This will return instantly
 */
public fun <R> Mono.Companion.just(e :R):Mono<R>{
    return ValueMono(e)
}

/**
 * Returns a mono for the callable
 */
public fun <R> Mono.Companion.fromCallable(processor :() -> R):Mono<R>{
    return CallableMono(processor)
}

/**
 * Returns a mono that runs the processor when blocked
 */
public fun Mono.Companion.fromRunnable(processor: () -> Unit): Mono<Unit> {
    return CallableMono(processor)

}

/**
 * Returns a mono which will concatenate A and the result of B when called
 */
public fun <A,B,C> Mono.Companion.zip(a :A,b :Mono<B>,c :(A,B) -> C):Mono<C>{
    return zip(Mono.just(a),b,c)
}

/**
 * Returns a mono which will concatenate the results of A and B when called
 */
public fun <A,B,C> Mono.Companion.zip(a :Mono<A>,b :Mono<B>,c :(A,B) -> C):Mono<C>{
    return CallableMono { c(a.block(),b.block()) }
}
