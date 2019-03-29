package systems.carson.mono.impl

import systems.carson.EndResult
import systems.carson.impl.closed
import systems.carson.impl.forValue
import systems.carson.mono.ClosableMono

internal class ValueMono<R>(private val value :R) : GenericMono<R>(), ClosableMono<R> {
    override fun get(): EndResult<R> {
        if(canceled)
            return EndResult.closed()
        gotten = true
        return EndResult.forValue(value)
    }

    private var canceled = false
    private var gotten = false
    override fun close() :Boolean{
        canceled = true
        return !gotten
    }
}