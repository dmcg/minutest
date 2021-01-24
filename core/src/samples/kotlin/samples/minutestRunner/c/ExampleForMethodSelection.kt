package samples.minutestRunner.c

import dev.minutest.rootContext
import dev.minutest.test2
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.platform.commons.annotation.Testable

class ExampleForMethodSelection {

    @Testable
    fun root1() = rootContext {
        test2("a passing test") {
            assertTrue(true, "example success")
        }
    }

    @Testable
    fun root2() = rootContext {
        test2("a passing test") {
            assertTrue(true, "example success")
        }
    }
}