package dev.minutest.experimental

import dev.minutest.Annotatable
import dev.minutest.ContextBuilder
import java.util.concurrent.ExecutorService

class ExecutorMarker(
    val executorService: ExecutorService?
)

fun ContextBuilder<*>.executor(executorService: ExecutorService?) {
    (this as Annotatable<*>).addMarker(ExecutorMarker(executorService))
}