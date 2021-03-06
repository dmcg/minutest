package dev.minutest.examples.fixtures

import dev.minutest.ContextBuilder
import dev.minutest.given
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import dev.minutest.test
import org.junit.jupiter.api.Assertions.assertEquals

class SelfDescribingFixtureExampleTests : JUnit5Minutests {

    data class Arguments(val description: String, val l: Int, val r: Int) {
        override fun toString() = description
    }

    fun tests() = rootContext<Arguments> {

        context(Arguments("positive positive", l = 3, r = 1)) {
            test("addition") {
                assertEquals(4, l + r)
            }
            test("subtraction") {
                assertEquals(2, l - r)
            }
        }

        context(Arguments("positive negative", l = 3, r = -1)) {
            test("addition") {
                assertEquals(2, l + r)
            }
            test("subtraction") {
                assertEquals(4, l - r)
            }
        }
    }
}

private fun <F> ContextBuilder<F>.context(f: F, builder: ContextBuilder<F>.() -> Unit) = context(f.toString()) {
    given { f }
    builder()
}
