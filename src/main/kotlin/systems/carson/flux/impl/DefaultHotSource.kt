package systems.carson.flux.impl

import systems.carson.flux.Flux
import systems.carson.flux.HotSource

internal class DefaultHotSource<R> : HotSource<R> {
    private val runners = mutableListOf<Runner<R>>()
    private val closers = mutableListOf<Closer>()
    private var closed = false

    override fun runOnGet(runner: (R) -> Unit) {
        runners.add(Runner(runner))
    }

    override fun runOnClose(runner: () -> Unit) {
        closers.add(Closer(runner))
    }

    fun submit(r :R){
        if(closed)throw IllegalAccessError("Can not submit to a closed hot source")
        runners.forEach {
            it.run(r)
        }
    }

    override fun isClosed(): Boolean {
        return closed
    }

    override fun close(){
        closed = true
        closers.forEach { it.run.invoke() }
    }

    override fun toFlux(): Flux<R> {
        return HotFlux(this)
    }

    private class Runner<R>(val run :(R) -> Unit)
    private class Closer(val run :() -> Unit)

}