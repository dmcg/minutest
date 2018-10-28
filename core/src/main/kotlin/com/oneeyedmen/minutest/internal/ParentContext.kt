package com.oneeyedmen.minutest.internal

interface ParentContext<F> {
    val name: String
    fun runTest(test: F.() -> F)
}