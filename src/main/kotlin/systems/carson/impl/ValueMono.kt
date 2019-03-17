package systems.carson.impl

import systems.carson.ClosableMono
import systems.carson.EndResult

internal class ValueMono<R>(private val value :R) :GenericMono<R>(),ClosableMono<R> {
    override fun get(): EndResult<R> {
        if(canceled)
            return EndResult.closed()
        return EndResult.forValue(value)
    }

    private var canceled = false
    override fun cancel() {
        canceled = true
    }
}