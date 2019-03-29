package systems.carson.mono.impl
import systems.carson.EndResult
import systems.carson.impl.closed
import systems.carson.impl.forValue
import systems.carson.mono.ClosableMono
import java.util.*

internal class PollingMono<R>(private val callable :() -> Optional<R>, private val millis :Long = 10) : GenericMono<R>(),
    ClosableMono<R> {
    private var closed = false
    private var processed = false
    override fun get(): EndResult<R> {
        while(true){
            if(closed)
                return EndResult.closed()
            val value = callable()
            if(value.isPresent) {
                processed = true
                return EndResult.forValue(value.get())
            }
            Thread.sleep(millis)
        }
    }

    override fun close():Boolean {
        closed = true
        return !processed
    }

}