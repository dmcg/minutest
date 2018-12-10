// explicit name for generated class so that tests are not coupled to the behaviour of the kotlin compiler
@file:JvmName("ExampleSkippedMinutest")

package example.a

import com.oneeyedmen.minutest.experimental.SKIP
import com.oneeyedmen.minutest.experimental.context
import com.oneeyedmen.minutest.experimental.skipAndFocus
import org.junit.jupiter.api.Assertions.fail


fun `example skipped context`() = SKIP - context<Unit>(skipAndFocus) {
    test("skip is honoured") {
        fail("skip wasn't honoured")
    }
}

