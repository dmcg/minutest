package uk.org.minutest

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.TestFactory
import uk.org.minutest.junit.toTestFactory


class FixtureSupplyingTests {

    @TestFactory fun `supply fixture at top`() = rootContext<String> {
        fixture { "banana" }
        context("parent had fixture") {
            test("test") {
                assertEquals("banana", this)
            }
        }
    }.toTestFactory()

    @TestFactory fun `supply fixture in derivedContext`() = rootContext<Unit> {
        derivedContext<String>("parent had no fixture") {
            fixture { "banana" }
            test("test") {
                assertEquals("banana", this)
            }
        }
    }.toTestFactory()

    @TestFactory fun `copes with no fixture if context has no operations`() = rootContext<Pair<Int, String>> {
        context("supplies the fixture") {
            fixture {
                42 to "the answer"
            }
            test("test") {
                assertEquals(42, this.first)
            }
        }
    }.toTestFactory()
}