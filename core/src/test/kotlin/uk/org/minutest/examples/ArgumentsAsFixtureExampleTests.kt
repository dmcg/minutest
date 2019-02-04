package uk.org.minutest.examples

import org.junit.jupiter.api.Assertions.assertEquals
import uk.org.minutest.junit.JUnit5Minutests
import uk.org.minutest.rootContext

class ArgumentsAsFixtureExampleTests : JUnit5Minutests {

    data class Arguments(val l: Int, val r: Int)

    override val tests = rootContext<Arguments> {

        context("positive positive") {
            fixture {
                Arguments(l = 3, r = 1)
            }
            test("addition") {
                assertEquals(4, l + r)
            }
            test("subtraction") {
                assertEquals(2, l - r)
            }
        }

        context("positive negative") {
            fixture {
                Arguments(l = 3, r = -1)
            }
            test("addition") {
                assertEquals(2, l + r)
            }
            test("subtraction") {
                assertEquals(4, l - r)
            }
        }
    }
}