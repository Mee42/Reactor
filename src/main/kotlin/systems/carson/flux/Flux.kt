package systems.carson.flux

import java.util.*

interface Flux<R>{

    fun <T> map(processor :(R) -> T) : Flux<T>

    fun blockFirst() :R

    fun blockLast()

    fun take(n :Int): Flux<R>

    /** Block till one returns, or return Optional#empty if flux closes */
    fun blockFirstOptional() : Optional<R>

    companion object
}