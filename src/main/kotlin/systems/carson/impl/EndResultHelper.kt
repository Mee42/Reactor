package systems.carson.impl

import systems.carson.EndResult


internal fun <R> EndResult.Companion.closed() : EndResult<R> {
    return EndResultImpl(isClosed = true)
}
internal fun <R> EndResult.Companion.forValue(r :R): EndResult<R> {
    return EndResultImpl(value = r)
}