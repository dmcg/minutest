package dev.minutest.examples

import dev.minutest.ContextBuilder
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue

// A translation of FizzBuzz tested with JUnit theories -
// http://www.oneeyedmen.com/tdd-v-testing-part2.html
class TheoriesExampleTests : JUnit5Minutests {

    override val tests = rootContext<Unit> {
        (1..31).forEach { i ->

            // These theories will be checked, but no tests are actually created

            theory(i, "starts with Fizz", Condition("divisible by 3") { this % 3 == 0 }) {
                assertTrue(fizzBuzz(i).startsWith("Fizz"))
            }

            theory(i, "ends with Buzz", Condition("divisible by 5") { this % 5 == 0 }) {
                assertTrue(fizzBuzz(i).endsWith("Buzz"))
            }

            theory(i, "is string", Condition("other numbers") { this % 3 != 0 && this % 5 != 0 }) {
                assertEquals(i.toString(), fizzBuzz(i))
            }

            // When uncommented 2 failing tests are created -
            // is Fizz when divisible by 3 failed for value [15]
            // is Fizz when divisible by 3 failed for value [30]
            ignore {
                theory(i, "is Fizz", Condition("divisible by 3") { this % 3 == 0 }) {
                    assertEquals("Fizz", fizzBuzz(i))
                }
            }
        }
    }
}

fun fizzBuzz(i: Int): String = when {
    i % 15 == 0 -> "FizzBuzz"
    i % 3 == 0 -> "Fizz"
    i % 5 == 0 -> "Buzz"
    else -> i.toString()
}

fun <F> ContextBuilder<*>.theory(fixture: F, name: String, condition: Condition<F>, check: (F) -> Unit) {
    if (condition.appliesTo(fixture))
        try {
            check(fixture)
        } catch (throwable: Throwable) {
            test("$name when ${condition.name} failed for value [$fixture]") {
                throw throwable
            }
        }
}

data class Condition<F>(val name: String, val predicate: F.() -> Boolean) {
    fun appliesTo(fixture: F) = predicate(fixture)
}

@Suppress("UNUSED_PARAMETER")
fun ignore(f: () -> Unit) {
}
