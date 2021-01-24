
package samples.minutestRunner.a

import dev.minutest.rootContext
import dev.minutest.test2
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.platform.commons.annotation.Testable

class ExampleMinutestInClass {

    @Testable
    fun `example context in class`() = rootContext {
        test2("a passing test") {
            assertTrue(true, "example success")
        }
    }
}