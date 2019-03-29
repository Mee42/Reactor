package systems.carson.funs

import systems.carson.EndResult
import systems.carson.flux.Flux
import systems.carson.flux.HotSource
import systems.carson.flux.impl.DefaultHotSource
import systems.carson.flux.impl.GenericFlux
import systems.carson.flux.impl.GenericStreamProducer
import systems.carson.flux.impl.StaticStreamProducer
import systems.carson.impl.ThreadManager
import systems.carson.impl.closed
import systems.carson.impl.forValue

fun <R> Flux.Companion.just(r :R): Flux<R> {
    return GenericFlux(StaticStreamProducer(listOf(r)))
}

fun <R> Flux.Companion.just(r :List<R>): Flux<R> {
    return GenericFlux(StaticStreamProducer(r))
}

fun <R> Flux.Companion.fromNullableCallable(producer :() -> R?): Flux<R> {
    return GenericFlux(GenericStreamProducer {
        val value = producer()
        if (value == null)
            EndResult.closed()
        else
            EndResult.forValue(value)
    })
}

fun <R> HotSource.Companion.create(producer :() -> R?):HotSource<R>{
    val hot = DefaultHotSource<R>()
    ThreadManager.threadPool.execute {
        while(!hot.isClosed()){
            val x =producer.invoke()
            x?.let { hot.submit(it) }
        }
    }
    return hot
}