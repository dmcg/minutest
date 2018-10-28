package com.oneeyedmen.minutest.internal

internal class Operations<F> {
    val befores = mutableListOf<(F) -> Unit>()
    val afters = mutableListOf<(F) -> Unit>()

    // apply befores in order - if anything is thrown return it and the last successful value
    fun applyBeforesTo(fixture: F): OpResult<F> {
        befores.forEach { beforeFn ->
            try {
                beforeFn(fixture)
            }
            catch (t: Throwable) {
                return OpResult(t, fixture)
            }
        }
        return OpResult(null, fixture)
    }
    
    fun applyAftersTo(fixture: F) {
        afters.forEach {
            afterFn -> afterFn(fixture)
        }
    }
}
