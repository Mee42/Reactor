package systems.carson.impl

import systems.carson.BlockWhenClosedException
import systems.carson.EndResult
import systems.carson.Mono
import java.time.Duration
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.reflect.KClass

internal abstract class GenericMono<R> : Mono<R>{

    override fun <E> map(processor: (R) -> E): Mono<E> {
        return CallableMono(ComplexProducer {
            val value = get()
            if (value.isClosed())
                return@ComplexProducer EndResultImpl<E>(isClosed = true)
            return@ComplexProducer EndResultImpl(processor(value.value()))
        })
    }

    override fun block(): R {
        return get().valueOptional().orElseThrow { BlockWhenClosedException() }
    }


    override fun <E> flatMap(processor: (R) -> Mono<E>): Mono<E> {
        return CallableMono(ComplexProducer {
            val get = get()
            if (get.isClosed())
                return@ComplexProducer EndResultImpl<E>(isClosed = true)
            val value = get.value()
            val newValue: Mono<E> = processor(value)
            val newGet = newValue.get()
            if (newGet.isClosed())
                return@ComplexProducer EndResultImpl<E>(isClosed = true)
            return@ComplexProducer EndResultImpl(value = newGet.value())
        })
    }

    override fun doOnGet(processor: (R) -> Unit): Mono<R> {
        return CallableMono(ComplexProducer {
            val get = get()
            if (get.isClosed())
                return@ComplexProducer EndResultImpl<R>(isClosed = true)
            processor(get.value())
            return@ComplexProducer EndResultImpl(value = get.value())
        })
    }

    override fun <E : Throwable> doOnError(error: KClass<E>, errorHandler: (E) -> R): Mono<R> {
        return CallableMono(ComplexProducer r@{
            try {
                val get = get()
                if (get.isClosed())
                    return@r EndResultImpl<R>(isClosed = true)
                return@r EndResultImpl(value = get.value())
            } catch (e: Throwable) {
                if (error.isInstance(e)) {
                    @Suppress("UNCHECKED_CAST")
                    return@r EndResultImpl(value = errorHandler(e as E))
                } else {
                    throw e
                }
            }
        })
    }

    override fun delay(duration: Duration): Mono<R> {
        return CallableMono(ComplexProducer r@{
            val get = get()
            Thread.sleep(duration.toMillis())
            return@r get
        })
    }

    override fun filter(predicate: (R) -> Boolean): Mono<R> {
        return CallableMono(ComplexProducer r@{
            val get = get()
//            if(get.isClosed)
//                return@r EndResultImpl<R>(isClosed = true)
//            if(predicate(get.value!!))
            return@r when {
                get.isClosed() || !predicate(get.value()) -> EndResultImpl(isClosed = true)
                else -> EndResultImpl(value = get.value())
            }

        })
    }

    override fun ifClosed(defaultValue: () -> R): Mono<R> {
        return CallableMono(ComplexProducer r@{
            val get = get()
            if (get.isClosed())
                return@r EndResultImpl(value = defaultValue())
            return@r EndResultImpl(value = get.value())
        })
    }

    override fun blockOptional(): Optional<R> {
        val get = get()
        if(get.isClosed())return Optional.empty()
        return Optional.of(get.value())
    }

    override fun blockWithTimeout(timeout: Duration): Optional<R> {
        val future = ThreadManager.threadPool.submit(Callable { get() })
        return try{
            val x = future.get(timeout.toMillis(), TimeUnit.MILLISECONDS)
            if(x.isClosed())
                return Optional.empty()
            return Optional.of(x.value())
        }catch(e : TimeoutException){
            Optional.empty()
        }
    }

    override fun subscribe() {
        ThreadManager.threadPool.execute { this.get() }

    }

}