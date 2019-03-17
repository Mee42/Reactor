package systems.carson.impl

import systems.carson.ClosableMono
import systems.carson.EndResult
import java.util.*

internal class PollingMono<R>(private val callable :() -> Optional<R>, private val millis :Long = 10) : GenericMono<R>(),ClosableMono<R> {
    private var closed = false
    override fun get(): EndResult<R> {
        while(true){
            if(closed)
                return EndResult.closed()
            val value = callable()
            if(value.isPresent)
                return EndResult.forValue(value.get())
            Thread.sleep(millis)
        }
    }

    override fun cancel() {
        closed = true
    }

}