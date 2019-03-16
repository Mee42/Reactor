package systems.carson

import java.time.Duration
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Future
import java.util.concurrent.FutureTask
import kotlin.reflect.KClass

/***/
interface Mono<R> {

    /**
     * Returns a mono which transforms the original value into another
     * @param processor The processor which will transform the value
     * @returns A mono that will return the mutated value
     */
    fun <E> map(processor :(R) -> E):Mono<E>

    /**
     * Returns a mono which transforms the original value into another mono, and the executes that mono at block/subscribe time
     *
     * For example
     *
     * `val x = Mono.fromCallable { Mono.just(5).block() + 5}`
     *
     * can be replaced by
     *
     * `val x = Mono.just(5).flatMap { it + 5 }`
     *
     * @param processor The processor which will tranform the value
     * @returns A mono which will act like the result of the mutated value.
     */
    fun <E> flatMap(processor :(R) -> Mono<E>):Mono<E>

    /**
     * Preform action when a value passes through the mono
     *
     * @param processor The processor that will be executed when a value is passed throught the mono
     * @returns `this`
     */
    fun doOnGet(processor :(R) -> Unit):Mono<R>

    /**
     * If an exception is thrown that matches, use this callable instead
     *
     * @param error The type of error to handle
     * @param errorHandler The handler that will be called to supply an alternate value
     * @return A mono with the error handling above
     */
    fun <E :Throwable> doOnError(error : KClass<E>,errorHandler :(E) -> R):Mono<R>

    /**
     * Delays the mono a specified amount of time
     * @param duration The amount to delay
     * @returns A mono that will delay the specified duration before passing it through
     */
    fun delay(duration :Duration):Mono<R>

    /* Everything subsequent will actually execute user code */


    /**
     * Gets the item, waiting as long as necessary
     * @returns The item
     */
    fun block() :R


    /**
     * Gets the item with a specific timeout duration.
     * Returns Optional#empty if waiting lasts beyond the duration,
     * or if the mono closes
     * @param timeout The time to wait before returning Optional.empty()
     * @returns The value if it is returned in subsequent time, otherwise nothing
     */
    fun blockOptional(timeout : Duration): Optional<R>

    /**
     * Subscribes the mono to be executed
     */
    fun subscribe()

    companion object
}