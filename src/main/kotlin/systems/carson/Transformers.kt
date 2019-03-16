package systems.carson

fun <T> T.toMono():Mono<T>{
    return Mono.just(this)
}