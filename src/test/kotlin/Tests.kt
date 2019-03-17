import org.junit.Assert.assertEquals
import org.junit.Assert.fail
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
        val mono = a.flatMap { it.zipToMono(b) { a,b -> a + b} }
            .flatMap { it.zipToMono(c) {a,b -> a + b} }
            .flatMap { it.zipToMono(d) {a,b -> a + b} }

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
        assertEquals(Mono.just(5).block(),(5).toMono().block())
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


    @Test
    fun filter1(){
        var i = false
        val x = Mono.just(false)
            .filter { it }
            .doOnGet { i = true }
            .ifClosed { true }
            .block()
        assert(!i)
        assert(x)
    }

    @Test
    fun filter2(){
        val hello: Optional<String> = Mono.just("hello").filter { it == "hello" }.blockOptional()
        val world: Optional<String> = Mono.just("world").filter { it == "hello" }.blockOptional()
        assertEquals(Optional.of("hello"),hello)
        assertEquals(Optional.empty<String>(),world)
    }

    @Test
    fun ifClosed(){
        var i0 = false
        var i1 = false
        val hello: Optional<String> = Mono.just("hello").filter { it == "hello" }.doOnGet { i0 = true }.blockOptional()
        val world: Optional<String> = Mono.just("world").filter { it == "hello" }.doOnGet { i1 = true }.blockOptional()
        assert(i0)
        assert(!i1)
        assertEquals(Optional.of("hello"),hello)
        assertEquals(Optional.empty<String>(),world)
    }

    @Test
    fun failedBlock(){
        try{
            Mono.just("hello").filter { it == "world" }.block()
            fail("Block when closed exception was thrown")
        }catch(e : BlockWhenClosedException){}
    }
    @Test
    fun closedGetOpWithTimeout(){
        val mono = Mono.just("hello").filter { it == "world" }.blockWithTimeout(Duration.ofSeconds(1))
        assertEquals(Optional.empty<String>(),mono)
    }

    @Test
    fun closedZips(){
        val closed = Mono.just(false).filter { it }
        val open = Mono.just(true).filter { it }
        val and = Mono.zip(closed,open) { a,b -> a && b }.blockOptional()
        val or = Mono.zip(open,closed) { a,b -> a || b }.blockOptional()
        val orr = Mono.zip(open,open) { a,b -> a || b }.blockOptional()
        assertEquals(Optional.empty<Boolean>(),and)
        assertEquals(Optional.empty<Boolean>(),or)
        assertEquals(Optional.of(true),orr)


    }
}

class TimedTests{

    @Test
    fun test1(){
        val x = Mono.just(5).map { Thread.sleep(1000);it + 5 }
        val op = x.blockWithTimeout(Duration.ofMillis(500))
        assertEquals(Optional.empty<Int>(),op)
    }

    @Test
    fun test2(){
        val x = Mono.just(5).map { Thread.sleep(1000);it + 5 }
        val op = x.blockWithTimeout(Duration.ofSeconds(2))
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
        val mono: Optional<Int> = Mono.just(5).delay(Duration.ofSeconds(5)).blockWithTimeout(Duration.ofSeconds(10))
        assert(mono.isPresent)
    }

    @Test
    fun delay2(){
        val mono: Optional<Int> = Mono.just(5).delay(Duration.ofSeconds(5)).blockWithTimeout(Duration.ofSeconds(1))
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



}