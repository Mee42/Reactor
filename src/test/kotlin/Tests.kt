import org.junit.Assert.assertEquals
import org.junit.Test
import systems.carson.*
import java.lang.NullPointerException
import java.time.Duration
import java.util.*
import kotlin.reflect.KClass

class Tests{

    @Test
    fun just(){
        assertEquals(1,Mono.just(1).block())
    }

    @Test
    fun map(){
        assertEquals(2,Mono.just(1).map { it + 1 }.block())
    }


    @Test
    fun fromCallable(){
        assertEquals("hello_world",Mono.fromCallable { "hello" }.map { it + "_" }.map { it + "world" }.block())
    }

    @Test
    fun fromCallableCallingAtTheCorrectTime(){
        var x = false
        val y = Mono.fromCallable { x = true }
        assert(!x)
        y.block()
        assert(x)
    }

    @Test
    fun flatMap(){
        assertEquals("hello",Mono.fromCallable { "hel" }.flatMap { w -> Mono.fromCallable { w + "lo" } }.block())
    }

    @Test
    fun flatMapRunningAtTheRightTime(){
        var x = false
        val mono :Mono<String> = Mono.fromCallable { x = true;"hel" }.flatMap { w -> Mono.fromCallable { x = true;w + "lo" } }
        assert(!x)
        assertEquals("hello",mono.block())
        assert(x)
    }

    @Test
    fun flatMapAndZip(){
        val a = Mono.just("a")
        val b = Mono.just("b")
        val c = Mono.just("c")
        val d = Mono.just('d')
        val mono = a.flatMap { Mono.zip(it,b){ a,b -> a + b} }
            .flatMap { Mono.zip(it,c) {a,b -> a + b} }
            .flatMap { Mono.zip(it,d) {a,b -> a + b} }
        assertEquals("abcd",mono.block())
    }

    @Test
    fun flatMapAndExecuteTime(){
        var i = 0
        val a = Mono.fromCallable { i++;"a" }
        val b = Mono.fromCallable { i++;"b" }
        val c = Mono.fromCallable { i++;"c" }
        val d = Mono.fromCallable { i++;"d" }
        val mono = a
            .flatMap { b }
            .flatMap { c }
            .flatMap { d }
        assertEquals(0,i)
        assertEquals("d",mono.block())
        assertEquals(4,i)
    }

    @Test
    fun chainedFlatMap(){
        class D(val e :String)
        class C(val d :Mono<D>)
        class B(val c :Mono<C>)
        class A(val b :Mono<B>)
        val d = D("hello world")
        val c = C(Mono.fromCallable { d })
        val b = B(Mono.fromCallable { c })
        val a = A(Mono.fromCallable { b })
        val x = a.b.flatMap { it.c }.flatMap { it.d }.map { it.e }
        assertEquals("hello world",x.block())
    }


    @Test
    fun doOnGet(){
        var i = 0
        val x = Mono.just(10)
            .map { it + 10 }
            .doOnGet { i = it }
            .map { it + 10 }
            .block()
        assertEquals(20,i)
        assertEquals(30,x)
    }

    @Test
    fun error1(){
        var i = false
        val x = Mono.just("hello")
            .map { Integer.parseInt(it) }
            .doOnError(NumberFormatException::class) { i = true;-1 }
            .block()
        assert(i)
        assertEquals(-1,x)
    }

    @Test
    fun error2(){
        var i = false
        val x = Mono.just("23")
            .map { Integer.parseInt(it) }
            .doOnError(NumberFormatException::class) { i = true;-1 }
            .block()
        assert(!i)
        assertEquals(23,x)
    }

    @Test
    fun error3(){
        var i = Optional.empty<Boolean>()
        var x = 23
        try {
            x = Mono.just("hello")
                .map { Integer.parseInt(it) }
                .doOnError(Error::class) { i = Optional.of(false);-1 }
                .block()
        }catch(e :NumberFormatException){
            i = Optional.of(true)
        }
        assert(i.isPresent)
        assert(i.get())
        assertEquals(23,x)
    }

    @Test
    fun error4(){

        open class C:RuntimeException()
        class A:C()
        class B:C()

        var called : KClass<out C>? = null
        @Suppress("UNREACHABLE_CODE") val x = Mono.just(0)
            .map { throw A();"" }
            .doOnError(A::class) { called = it::class;"hello" }
            .doOnError(B::class) { called = it::class;"world" }
            .block()
        assertEquals("hello",x)
        try{
            called!!
        }catch(e :NullPointerException){
            assert(false)
        }
        assertEquals(called,A::class)
    }

    @Test
    fun error5(){

        open class C:RuntimeException()
        class A:C()
        class B:C()

        var called : KClass<out C>? = null
        @Suppress("UNREACHABLE_CODE") val x = Mono.just(0)
            .map { throw B();"" }
            .doOnError(A::class) { called = it::class;"hello" }
            .doOnError(B::class) { called = it::class;"world" }
            .block()
        assertEquals("world",x)
        try{
            called!!
        }catch(e :NullPointerException){
            assert(false)
        }
        assertEquals(called,B::class)
    }

    @Test
    fun error6(){

        val x = Mono.just("hello")
            .map { it.toInt() }
            .map { "" + it }
            .doOnError(NumberFormatException::class) { "hello" }
            .doOnError(NumberFormatException::class) { "world" }
            .block()
        assertEquals("hello",x)
    }

    @Test
    fun toMono1(){
        assertEquals(Mono.just(5),(5).toMono())
    }
    @Test
    fun toMono2(){
        assertEquals(10,(5).toMono().map { it + 5 }.block())
    }


    @Test
    fun doOnGet2(){
        var i = -1
        Mono.just(5).map { it + 5 }.doOnGet { i = it }.block()
        assertEquals(10,i)
    }


}

class TimedTests{

    @Test
    fun test1(){
        val x = Mono.just(5).map { Thread.sleep(1000);it + 5 }
        val op = x.blockOptional(Duration.ofMillis(500))
        assertEquals(Optional.empty<Int>(),op)
    }

    @Test
    fun test2(){
        val x = Mono.just(5).map { Thread.sleep(1000);it + 5 }
        val op = x.blockOptional(Duration.ofSeconds(2))
        assertEquals(Optional.of(10),op)
    }
    @Test
    fun test3(){
        val x = Mono.just(5).map { Thread.sleep(1000);it + 5 }
        val op = x.block()
        assertEquals(10,op)
    }

    @Test
    fun delay1(){
        val mono: Optional<Int> = Mono.just(5).delay(Duration.ofSeconds(5)).blockOptional(Duration.ofSeconds(10))
        assert(mono.isPresent)
    }

    @Test
    fun delay2(){
        val mono: Optional<Int> = Mono.just(5).delay(Duration.ofSeconds(5)).blockOptional(Duration.ofSeconds(1))
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



}