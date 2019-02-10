package dev.minutest.experimental

import dev.minutest.ContextBuilder


fun <F, C : AutoCloseable> ContextBuilder<F>.autoClose(f: () -> C): Lazy<C> =
    lazy(f).also { lazyCloseable ->
        afterAll { if (lazyCloseable.isInitialized()) lazyCloseable.value.close() }
    }

