package com.oneeyedmen.minutest.experimental

import com.oneeyedmen.minutest.TestContext


fun <F, C : AutoCloseable> TestContext<F>.autoClose(f: () -> C): Lazy<C> =
    lazy(f).also { lazyCloseable ->
        afterAll { if (lazyCloseable.isInitialized()) lazyCloseable.value.close() }
    }

