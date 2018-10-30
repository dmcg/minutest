package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.Test

interface ParentContext<F> {
    val name: String
    fun runTest(test: Test<F>)
}