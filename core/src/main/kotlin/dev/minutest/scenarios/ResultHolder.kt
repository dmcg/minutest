package dev.minutest.scenarios

// Required because you cannot have a nullable lateinit
class ResultHolder<R> {
    private val _value = ArrayList<R>(1)

    var value: R
        get() {
            check(_value.isNotEmpty()) { "Result has not been initialised" }
            return _value.first()
        }
        set(value) {
            check(_value.isEmpty()) { "Result has already been initialised" }
            _value.add(value)
        }
}