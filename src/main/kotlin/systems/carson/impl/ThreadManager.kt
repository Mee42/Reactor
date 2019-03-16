package systems.carson.impl

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

internal object ThreadManager {
    val threadPool: ExecutorService = Executors.newCachedThreadPool()
}