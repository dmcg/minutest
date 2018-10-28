package com.oneeyedmen.minutest.internal

internal data class OpResult<F>(val t: Throwable?, val lastValue: F) {
    fun flatMap(f: (F) -> OpResult<F>): OpResult<F> =
        if (t != null) this
        else f(this.lastValue)
    
    fun tryMap(f: (F)->F) =
        flatMap {
            try {
                OpResult(null, f(it))
            }
            catch (t: Throwable) {
                OpResult(t, it)
            }
        }
    
    fun maybeThrow() {
        if (t != null) {
            throw t
        }
    }
}

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
