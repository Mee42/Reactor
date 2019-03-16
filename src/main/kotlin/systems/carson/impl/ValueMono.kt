package systems.carson.impl

import java.time.Duration
import java.util.*

internal class ValueMono<R>(private val value :R): GenericMono<R>() {

    override fun block(): R {
        return value
    }

    override fun blockOptional(timeout: Duration): Optional<R> {
        return Optional.of(value)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ValueMono<*>

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        return value?.hashCode() ?: 0
    }
}