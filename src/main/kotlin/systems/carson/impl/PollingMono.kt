package systems.carson.impl

import systems.carson.ClosableMono
import systems.carson.EndResult
import java.util.*

internal class PollingMono<R>(private val callable :() -> Optional<R>, private val millis :Long = 10) : GenericMono<R>(),ClosableMono<R> {
    private var closed = false
    override fun get(): EndResult<R> {
        while(true){
            if(closed)
                return EndResultImpl(isClosed = true)
            val value = callable()
            if(value.isPresent)
                return EndResultImpl(value = value.get())
            Thread.sleep(millis)
        }
    }

    override fun cancel() {
        closed = true
    }

}