package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.Named
import com.oneeyedmen.minutest.Test

interface ParentContext<F> : Named {
    fun runTest(test: Test<F>)
}
