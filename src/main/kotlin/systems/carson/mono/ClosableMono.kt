package systems.carson.mono

/**
 * A [Mono] that can be closed
 */
interface ClosableMono<R> : Mono<R> {
    /**
     * This will close any existing block or subscribe unless it has already been
     * requested.
     * @returns True if the [Mono] is cancelled successfully
     */
    fun close():Boolean
    companion object
}