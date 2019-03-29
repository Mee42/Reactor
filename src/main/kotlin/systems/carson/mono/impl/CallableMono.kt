package systems.carson.mono.impl

import systems.carson.EndResult


internal class CallableMono<R>(private val producer : Producer<R>) : GenericMono<R>() {

    override fun get() : EndResult<R> {
        return producer.get()
    }

}