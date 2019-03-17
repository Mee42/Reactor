package systems.carson.impl

import systems.carson.ClosableMono
import systems.carson.EndResult

internal class ValueMono<R>(private val value :R) :GenericMono<R>(),ClosableMono<R> {
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