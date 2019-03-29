package systems.carson.flux.impl

import systems.carson.BlockWhenClosedException
import systems.carson.EndResult
import systems.carson.flux.Flux
import systems.carson.flux.HotSource
import systems.carson.impl.closed
import systems.carson.impl.forValue
import java.util.*

class HotFlux<R>(private val hot : HotSource<R>): Flux<R> {

    override fun <T> map(processor: (R) -> T): Flux<T> {
        var closed = false
        val queue = mutableListOf<T>()
        hot.runOnClose { closed = true }
        hot.runOnGet { queue.add(processor(it)) }
        return GenericFlux(PollingStreamProducer {
            when{
                closed -> EndResult.closed()
                queue.isNotEmpty() -> EndResult.forValue(queue.removeAt(0))
                else -> null
            }
        })

    }

    override fun blockFirst(): R {
        var value :R? = null
        var close = false
        hot.runOnGet { value = it }
        hot.runOnClose { close = true }
        while(value == null && !close);
        return if(close)
            throw BlockWhenClosedException()
        else
            value!!
    }

    override fun blockLast() {
        var close = true
        hot.runOnClose { close = false }
        while(close);
    }

    override fun take(n: Int): Flux<R> {
        var count = 0
        val queue = mutableListOf<EndResult<R>>()

        hot.runOnGet { count++;queue.add(EndResult.forValue(it)) }

        return GenericFlux(
            PollingStreamProducer {
                when {
                    count == n -> EndResult.closed()
                    hot.isClosed() -> EndResult.closed()
                    queue.isNotEmpty() -> queue.removeAt(0)
                    else -> null
                }

            }
        )
    }

    override fun blockFirstOptional(): Optional<R> {
        var value :R? = null
        var closed = false
        hot.runOnGet { value = it }
        hot.runOnClose { closed = true }
        while(value == null && !closed);
        return when{
            closed -> Optional.empty()
            else -> Optional.of(value!!)
        }
    }
}