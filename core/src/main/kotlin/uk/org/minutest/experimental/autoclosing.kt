package uk.org.minutest.experimental


fun <F, C : AutoCloseable> uk.org.minutest.ContextBuilder<F>.autoClose(f: () -> C): Lazy<C> =
    lazy(f).also { lazyCloseable ->
        afterAll { if (lazyCloseable.isInitialized()) lazyCloseable.value.close() }
    }

