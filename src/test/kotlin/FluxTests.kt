import org.junit.Assert.assertEquals
import org.junit.Test
import systems.carson.BlockWhenClosedException
import systems.carson.flux.Flux
import systems.carson.flux.HotSource
import systems.carson.funs.*
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.concurrent.thread

class FluxTests {
    @Test
    fun flux1(){
        var value = 0
        val x = Flux.just(10)
            .map { value = it;20 }
            .blockFirst()
        assertEquals(10,value)
        assertEquals(20,x)
    }

    @Test
    fun flux101(){

        var value = 0
        var i = 0
        val hotSource = HotSource.create { i++ }
        val x = hotSource.toFlux()
            .take(1)
            .map { value = 10;20 }
            .blockFirst()
        assertEquals(10,value)
        assertEquals(20,x)
    }

    @Test
    fun fluxidk(){
        var called = false
        val hot = HotSource.create { null }
        hot.close()
        try {
            hot.toFlux().map { called = false }.blockFirst()
        }catch(e :BlockWhenClosedException){
            called = true
        }
        assert(called)

    }

    @Test
    fun flux2(){
        var x = 0
        var string = ""
        Flux.fromNullableCallable { if(x < 10) x++ else null }
            .map { string+=it }
            .blockLast()
        assertEquals("0123456789",string)
    }


    @Test
    fun flux3(){
        var x = 0
        var string = ""
        Flux.fromNullableCallable { if(x < 10) x++ else null }
            .map { string+=it }
            .blockFirst()
        assertEquals("0",string)
    }

    @Test
    fun flux4(){
        var x = 0
        var string = ""
        Flux.fromNullableCallable { if(x < 10) x++ else null }
            .map { string+=it }
            .take(4)
            .blockLast()
        assertEquals("0123",string)
    }

    @Test
    fun flux5_callable_only_running_take_times(){
        var x = 0
        var called = 0
        var string = ""
        Flux.fromNullableCallable {
            called++
            if(x < 10)
                x++
            else
                null
        }
            .map { string+=it }
            .take(4)
            .blockLast()
        assertEquals("0123",string)
        assertEquals(4,called)
    }

    @Test
    fun flux6(){
        var string = ""
        Flux.just(listOf(0,1,2,3,4,5))
            .map { string+=it }
            .blockLast()
        assertEquals("012345",string)
    }

    @Test
    fun blockFirstOpOp(){
        val x = Flux.just(1).take(0).blockFirstOptional()
        assertEquals(Optional.empty<Int>(),x)
    }

    @Test
    fun blockFirstOpNonOp(){
        val x = Flux.just(1).take(1).blockFirstOptional()
        assertEquals(Optional.of(1),x)
    }

    @Test
    fun afsd(){
        
    }


}