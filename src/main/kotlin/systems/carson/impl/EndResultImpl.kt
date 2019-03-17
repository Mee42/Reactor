package systems.carson.impl

import systems.carson.EndResult
import java.util.*


internal class EndResultImpl<R>(val value :R? = null,
                                private val isClosed :Boolean = false) :EndResult<R>{
    init {
        if(isClosed && value != null)
            error("value in nonnull, but the end result is closed")
        if(!isClosed && value == null)
            error("value is null, but end result is not closed")
    }

    override fun isClosed(): Boolean {
        return isClosed
    }

    override fun value(): R {
        return value!!
    }

    override fun valueOptional(): Optional<R> {
        return Optional.ofNullable(value)
    }
}
