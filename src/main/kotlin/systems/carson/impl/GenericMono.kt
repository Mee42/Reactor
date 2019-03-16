package systems.carson.impl

import systems.carson.Mono
import java.time.Duration
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.reflect.KClass

abstract class GenericMono<R> : Mono<R> {


    override fun <E> map(processor: (R) -> E): Mono<E> {
        return CallableMono { processor(block()) }
    }

    override fun <E> flatMap(processor: (R) -> Mono<E>): Mono<E> {
        return  CallableMono { processor(block()).block() }
    }

    override fun blockOptional(timeout: Duration): Optional<R> {
        val future : Future<R> = Executors.newSingleThreadExecutor().submit(Callable { block() })
        return try{
            val x :R = future.get(timeout.toMillis(), TimeUnit.MILLISECONDS)
            Optional.of(x)
        }catch(e : TimeoutException){
            Optional.empty()
        }
    }

    override fun doOnGet(processor: (R) -> Unit): Mono<R> {
        return CallableMono {
            val value = block()
            processor(value)
            return@CallableMono value
        }
    }

    override fun delay(duration: Duration): Mono<R> {
        return CallableMono {
            val x = block()
            Thread.sleep(duration.toMillis())
            x
        }
    }

    override fun <E : Throwable> doOnError(error: KClass<E>, errorHandler: (E) -> R): Mono<R> {
        return CallableMono {
            try{
                block()
            }catch(e :Throwable){
                if(error.isInstance(e)){
                    @Suppress("UNCHECKED_CAST")
                    errorHandler(e as E)
                }else {
                    throw e
                }
            }
        }
    }

    override fun subscribe() {
        ThreadManager.threadPool.execute { this.block() }
    }
}