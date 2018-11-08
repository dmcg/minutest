package com.oneeyedmen.minutest.internal


/**
 * The result of applying a sequence of operations, where an operation might fail and we want to know the exception
 * thrown and the value before it was.
 */
internal data class OpResult<F>(val t: Throwable?, val lastValue: F) {
    fun flatMap(f: (F) -> OpResult<F>): OpResult<F> =
        if (t != null) this
        else f(this.lastValue)

    fun tryMap(f: (F) -> F) =
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
    
    fun orThrow(): F {
        maybeThrow()
        return lastValue
    }
    
    inline fun onLastValue(f: (F) -> Unit) =
        apply { f(lastValue) }
}