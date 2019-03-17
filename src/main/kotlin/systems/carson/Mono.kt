package systems.carson

import java.time.Duration
import java.util.*
import kotlin.reflect.KClass

/**
 * A representation of a promise of 0 or 1 values
 */
interface Mono<R> {

    /**
     * Returns a [Mono] which transforms the original value into another
     * @param processor The processor which will transform the value
     * @returns A [Mono] that will return the mutated value
     */
    fun <E> map(processor :(R) -> E):Mono<E>

    /**
     * Returns a [Mono] which transforms the original value into another [Mono], and the executes that [Mono] at block/subscribe time
     *
     * For example
     *
     * `val x = Mono.fromCallable { Mono.just(5).block() + 5}`
     *
     * can be replaced by
     *
     * `val x = Mono.just(5).flatMap { it + 5 }`
     *
     * @param processor The processor which will transform the value
     * @returns A [Mono] which will act like the result of the mutated value.
     */
    fun <E> flatMap(processor :(R) -> Mono<E>):Mono<E>

    /**
     * Preform action when a value passes through the [Mono]
     *
     * @param processor The processor that will be executed when a value is passed through the [Mono]
     * @returns A [Mono] which will call the processor when a value is passed through
     */
    fun doOnGet(processor :(R) -> Unit):Mono<R>

    /**
     * If an exception is thrown that matches [error][KClass], use this callable instead
     *
     * @param error The type of error to handle
     * @param errorHandler The handler that will be called to supply an alternate value
     * @return A [Mono] with the error handling above
     */
    fun <E :Throwable> doOnError(error : KClass<E>,errorHandler :(E) -> R):Mono<R>

    /**
     * Returns a [Mono] that will hold a single for a specified amount of time
     * @param duration The amount to delay
     * @returns A [Mono] that will delay the specified duration before passing it through
     */
    fun delay(duration :Duration):Mono<R>

    /**
     * If the given predicate is false on call time, the returning [Mono] will not execute subsequent calls.
     * Otherwise, this returns itself
     *
     * @param predicate The predict for filtering
     * @returns A closed [Mono] if the predict returns true
     */
    fun filter(predicate :(R) -> Boolean):Mono<R>

    /**
     * If the [Mono] is currently closed, use this value instead
     * @param defaultValue The processor to generate a default value
     * @returns An [Mono] which is definitely not closed
     */
    fun ifClosed(defaultValue :() -> R):Mono<R>




    /* Everything subsequent will actually execute user code */


    /**
     * Gets the item, waiting as long as necessary.
     * This will throw an exception if the [Mono] has been closed
     * @returns The value
     */
    fun block() :R

    /**
     * Returns R if returned, Optional#empty if the [Mono] has been closed
     * @returns Optional#empty if [Mono] has been closed, otherwise the blocked value
     */
    fun blockOptional(): Optional<R>


    /**
     * Gets the item with a specific timeout duration.
     * Returns Optional#empty if waiting lasts beyond the duration,
     * or if the [Mono] is closed
     * @param timeout The time to wait before returning Optional#empty
     * @returns The value if it is returned in subsequent time, otherwise nothing
     */
    fun blockWithTimeout(timeout : Duration): Optional<R>

    /**
     * Subscribes the [Mono] to be executed.
     * This will work on closed and unclosed [Mono]s
     */
    fun subscribe()


    /**
     * Gets the EndResult of the [Mono].
     * @suppress THIS SHOULD NOT BE USED
     */
     fun get(): EndResult<R>


    companion object
}