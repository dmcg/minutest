package samples.minutestRunner.c

import dev.minutest.rootContext
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.platform.commons.annotation.Testable

class ExampleForMethodSelection {

    @Testable
    fun root1() = rootContext {
        test("a passing test") {
            assertTrue(true, "example success")
        }
    }

    @Testable
    fun root2() = rootContext {
        test("a passing test") {
            assertTrue(true, "example success")
        }
    }
}