package systems.carson.mono.impl

import systems.carson.EndResult
import systems.carson.impl.forValue


internal interface Producer<R>{
    fun get(): EndResult<R>
}

internal class SimpleProducer<R>(private val r:() -> R): Producer<R> {
    override fun get(): EndResult<R> {
        return EndResult.forValue(r())
    }
}
internal class ComplexProducer<R>(private val r:() -> EndResult<R>): Producer<R> {
    override fun get(): EndResult<R> {
        return r()
    }
}