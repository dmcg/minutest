package dev.minutest.examples

class Calculator {
    var currentValue: Int = 0

    fun add(a: Int) {
        currentValue += a
    }

    fun subtract(a: Int) {
        currentValue -= a
    }
}
