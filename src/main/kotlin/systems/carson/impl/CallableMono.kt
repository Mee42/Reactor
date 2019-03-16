package systems.carson.impl

internal class CallableMono<R>(private val callable :() -> R):GenericMono<R>(){
    override fun block(): R {
        return callable()
    }
}