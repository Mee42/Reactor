package systems.carson

import systems.carson.impl.CallableMono
import systems.carson.impl.ComplexProducer
import systems.carson.impl.EndResultImpl
import systems.carson.impl.SimpleProducer

/**
 * Returns a [Mono] for the value.
 *
 */
fun <R> Mono.Companion.just(e :R):Mono<R>{
    return CallableMono(SimpleProducer { e })
}


/**
 * Returns a [Mono] for the callable
 */
fun <R> Mono.Companion.fromCallable(processor :() -> R):Mono<R>{
    return CallableMono(SimpleProducer(processor))
}


/**
 * Returns a [Mono] which will concatenate the results of A and B when called
 */
fun <A,B,C> Mono.Companion.zip(a :Mono<A>,b :Mono<B>,c :(A,B) -> C):Mono<C>{
    return CallableMono(ComplexProducer {
        val aResult = a.get()
        if (aResult.isClosed())
            return@ComplexProducer EndResultImpl<C>(isClosed = true)
        val bResult = b.get()
        if (bResult.isClosed())
            return@ComplexProducer EndResultImpl<C>(isClosed = true)
        val cReturn: C = c(aResult.value(), bResult.value())
        return@ComplexProducer EndResultImpl(value = cReturn)
    })
}