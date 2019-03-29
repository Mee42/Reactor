package systems.carson.funs

import systems.carson.EndResult
import systems.carson.impl.closed
import systems.carson.impl.forValue
import systems.carson.mono.ClosableMono
import systems.carson.mono.Mono
import systems.carson.mono.impl.*
import java.util.*

/**
 * Returns a [ClosableMono] for the value.
 * @param value The value to use
 * @returns A [ClosableMono] containing the value
 */
fun <R> Mono.Companion.just(value :R): ClosableMono<R> {
    return ValueMono(value = value)
}


/**
 * Returns a [Mono] for the callable
 */
fun <R> Mono.Companion.fromCallable(processor :() -> R): Mono<R> {
    return CallableMono(SimpleProducer(processor))
}


/**
 * Returns a mono that will poll the processor until it returns
 */
fun <R> Mono.Companion.fromPollable(millis :Long = 10, processor: () -> Optional<R>) : ClosableMono<R> {
    return PollingMono(processor,millis)
}


/**
 * Returns a [Mono] which will concatenate the results of A and B when called
 */
fun <A,B,C> Mono.Companion.zip(a : Mono<A>, b : Mono<B>, c :(A, B) -> C): Mono<C> {
    return CallableMono(ComplexProducer {
        val aResult = a.get()
        if (aResult.isClosed())
            return@ComplexProducer EndResult.closed<C>()
        val bResult = b.get()
        if (bResult.isClosed())
            return@ComplexProducer EndResult.closed<C>()
        val cReturn: C = c(aResult.value(), bResult.value())
        return@ComplexProducer EndResult.forValue(cReturn)
    })
}