package dev.minutest.internal


/**
 * The result of applying a sequence of operations, where an operation might fail and we want to know the exception
 * thrown and the value before it was.
 */
internal data class SequenceResult<F>(val t: Throwable?, val lastValue: F) {

    fun flatMap(f: (F) -> SequenceResult<F>): SequenceResult<F> = if (t != null) this else f(this.lastValue)

    fun tryMap(f: (F) -> F) =
        flatMap {
            try {
                SequenceResult(null, f(it))
            }
            catch (t: Throwable) {
                SequenceResult(t, it)
            }
        }

    fun orThrow(): F {
        if (t != null) {
            throw t
        }
        return lastValue
    }
    
    inline fun onLastValue(f: (F) -> Unit) =
        apply { f(lastValue) }
}