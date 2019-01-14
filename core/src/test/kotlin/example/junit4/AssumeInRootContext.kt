package example.junit4

import com.oneeyedmen.minutest.junit.JUnit4Minutests
import com.oneeyedmen.minutest.rootContext
import org.junit.Assert
import org.junit.Assume

class AssumeInRootContext : JUnit4Minutests() {
    fun tests() = rootContext<Unit> {
        test("assume in a test aborts it") {
            Assume.assumeTrue("black".toLowerCase() == "white")
            Assert.fail("shouldn't get here")
        }
    }
}