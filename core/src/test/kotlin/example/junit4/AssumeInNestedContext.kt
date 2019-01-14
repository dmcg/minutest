package example.junit4

import com.oneeyedmen.minutest.junit.JUnit4Minutests
import com.oneeyedmen.minutest.rootContext
import org.junit.Assert
import org.junit.Assume

class AssumeInNestedContext : JUnit4Minutests() {
    fun tests() = rootContext<Unit> {
        context("a context with assume in a fixture block") {
            modifyFixture {
                Assume.assumeTrue("black".toLowerCase() == "white")
            }
            test("should not be run") {
                Assert.fail("shouldn't get here")
            }
        }
    }
}