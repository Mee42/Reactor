package systems.carson.flux

interface HotSource<R>{
    fun runOnGet(runner :(R) -> Unit)
    fun runOnClose(runner :() -> Unit)
    fun toFlux() :Flux<R>
    fun isClosed() :Boolean
    fun close()


    companion object
}