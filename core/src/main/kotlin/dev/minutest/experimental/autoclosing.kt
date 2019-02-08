package dev.minutest.experimental


fun <F, C : AutoCloseable> dev.minutest.ContextBuilder<F>.autoClose(f: () -> C): Lazy<C> =
    lazy(f).also { lazyCloseable ->
        afterAll { if (lazyCloseable.isInitialized()) lazyCloseable.value.close() }
    }

