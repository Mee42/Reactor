package systems.carson.flux.impl

import systems.carson.EndResult
import systems.carson.flux.Flux
import systems.carson.impl.closed
import systems.carson.impl.forValue
import java.util.*

internal class GenericFlux<R>(private val producer : StreamProducer<R>) : Flux<R> {
    override fun <T> map(processor: (R) -> T): Flux<T> {
        return GenericFlux(
            GenericStreamProducer {
                val value = producer.get()
                if (value.isClosed())
                    EndResult.closed()
                else
                    EndResult.forValue(processor(value.value()))
            }
        )
    }

    override fun blockFirstOptional(): Optional<R> {
        return producer.get().valueOptional()
    }

    override fun blockFirst(): R {
        val value = producer.get()
        return value.value()//will throw an exception
    }

    override fun blockLast() {
        while(!producer.get().isClosed());
    }

    override fun take(n: Int): Flux<R> {
        var count = 0//weird place to put a counter
        return GenericFlux(
            GenericStreamProducer {
                if (count == n)
                    EndResult.closed()
                else {
                    val value = producer.get()
                    count++
                    value
                }

            }
        )
    }
}