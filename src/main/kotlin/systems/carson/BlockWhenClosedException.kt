package systems.carson

import java.lang.RuntimeException

/**
 * This exception is thrown when a closed mono is blocked
 */
class BlockWhenClosedException :RuntimeException()