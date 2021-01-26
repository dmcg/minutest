@file:Suppress("unused")

package dev.minutest.internal

import java.time.Duration
import java.time.Instant

internal inline fun <T> time(
    prefix: String,
    printer: (String) -> Unit = ::println,
    f: () -> T
): T {
    val start = Instant.now()
    return try {
        f()
    } finally {
        printer(prefix + Duration.between(start, Instant.now()))
    }
}

internal fun <T> T.printed(printer: (Any?) -> Unit = ::println): T {
    return this.also { printer(this) }
}