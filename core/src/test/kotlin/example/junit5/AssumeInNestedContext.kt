package example.junit5

import com.oneeyedmen.minutest.junit.JUnit5Minutests
import com.oneeyedmen.minutest.rootContext
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assumptions

class AssumeInNestedContext : JUnit5Minutests {
    override val tests = rootContext<Unit> {
        context("a context with assume in a fixture block") {
            modifyFixture {
                Assumptions.assumeTrue("black".toLowerCase() == "white")
            }
            test("should not be run") {
                Assertions.fail<Nothing>("shouldn't get here")
            }
        }
    }
}
