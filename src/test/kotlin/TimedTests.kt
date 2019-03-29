import org.junit.Assert
import org.junit.Test
import systems.carson.funs.*
import systems.carson.mono.Mono
import java.time.Duration
import java.util.*

class TimedTests{

    @Test
    fun test1(){
        val x = Mono.just(5).map { Thread.sleep(1000);it + 5 }
        val op = x.blockWithTimeout(Duration.ofMillis(500))
        Assert.assertEquals(Optional.empty<Int>(), op)
    }

    @Test
    fun test2(){
        val x = Mono.just(5).map { Thread.sleep(1000);it + 5 }
        val op = x.blockWithTimeout(Duration.ofSeconds(2))
        Assert.assertEquals(Optional.of(10), op)
    }
    @Test
    fun test3(){
        val x = Mono.just(5).map { Thread.sleep(1000);it + 5 }
        val op = x.block()
        Assert.assertEquals(10, op)
    }
    @Test
    fun test4(){
        val x  = Mono.just(5)
        x.close()
        val y  =  x.map { Thread.sleep(1000);it + 5 }
        val op = y.blockWithTimeout(Duration.ofMillis(10))
        Assert.assertEquals(Optional.empty<Int>(), op)
    }

    @Test
    fun delay1(){
        val mono: Optional<Int> = Mono.just(5).delay(Duration.ofSeconds(5)).blockWithTimeout(
            Duration.ofSeconds(10)
        )
        assert(mono.isPresent)
    }

    @Test
    fun delay2(){
        val mono: Optional<Int> = Mono.just(5).delay(Duration.ofSeconds(5)).blockWithTimeout(
            Duration.ofSeconds(1)
        )
        assert(!mono.isPresent)
    }

    @Test
    fun subscribe1(){
        val mono = Mono.just(5).map { it + 5 }
        var bool = false
        mono.doOnGet { bool = true }.subscribe()
        Thread.sleep(100)
        assert(bool)
    }

    @Test
    fun subscribe2(){
        var i = false
        val mono = Mono.just(5).map { it + 5 }
        mono.filter { it == -1 }.doOnGet { i = true }.subscribe()
        Thread.sleep(100)
        assert(!i)
    }


    @Test
    fun timed1(){
        fun getMonoA() = Mono.fromCallable { Thread.sleep(1000);"a" }
        fun getMonoB() = Mono.fromCallable { Thread.sleep(1000);"b" }
        fun getMonoC() = Mono.fromCallable { Thread.sleep(1000);"c" }
        fun getMonoD() = Mono.fromCallable { Thread.sleep(1000);"d" }
        var mono = ""
        getMonoA()
            .flatMap { it.zipToMono(getMonoB()){a,b -> a + b} }
            .flatMap { it.zipToMono(getMonoC()){a,b -> a + b} }
            .flatMap { it.zipToMono(getMonoD()){a,b -> a + b} }
            .doOnGet { mono = it }
            .subscribe()
        Thread.sleep(4_000)
        Assert.assertEquals("", mono)
        Thread.sleep(2_000)
        Assert.assertEquals("abcd", mono)
    }

}