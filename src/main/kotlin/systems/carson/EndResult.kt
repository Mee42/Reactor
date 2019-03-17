package systems.carson

import java.util.*

/**
 * An object that represents the end result of a [Mono]
 */
interface EndResult<R> {
    /**
     * Returns true if it is closed
     * @returns If closed
     */
    fun isClosed() :Boolean

    /**
     * Returns the value
     * @throws BlockWhenClosedException If closed
     * @returns The value
     */
    fun value() :R

    /**
     * Returns Optional.of(value), or Optional.empty() if closed
     * @returns A safe version of the value
     */
    fun valueOptional() : Optional<R>

    companion object
}