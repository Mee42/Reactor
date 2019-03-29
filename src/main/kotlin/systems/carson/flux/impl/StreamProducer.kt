package systems.carson.flux.impl

import systems.carson.EndResult
import systems.carson.impl.closed
import systems.carson.impl.forValue

interface StreamProducer<R> {
    /** Gets the next item, or blocks until it is received */
    fun get() : EndResult<R>
    /** Returns true if closed */
    fun isClosed() :Boolean
}

internal class GenericStreamProducer<R> (private val emitter :() -> EndResult<R>):
    StreamProducer<R> {
    var closed = false
    override fun get(): EndResult<R> {
        val value = emitter()
        if(value.isClosed())
            closed = true
        return value
    }

    override fun isClosed(): Boolean = closed
}

internal class StaticStreamProducer<R> (private val items :List<R>) : StreamProducer<R> {
    private var index = 0
    override fun get(): EndResult<R> {
        return if(index == items.size)
            EndResult.closed()
        else
            EndResult.forValue(items[index++])
    }

    override fun isClosed(): Boolean {
        return index == items.size
    }
}

internal class PollingStreamProducer<R> (private val callable :() -> EndResult<R>?):StreamProducer<R> {
    var closed = false

    override fun get(): EndResult<R> {
        var value :EndResult<R>? = null
        while(value == null){
            value = callable()
        }
        return value
    }

    override fun isClosed(): Boolean = closed
}