@file:Suppress("unused")

package dev.minutest.internal

import java.time.Duration
import java.time.Instant

internal inline fun <T> time(prefix: String, f: () -> T): T {
    val start = Instant.now()
    return try {
        f()
    } finally {
        println(prefix + Duration.between(start, Instant.now()))
    }
}

internal fun <T> T.printed() {
    this.also(::println)
}