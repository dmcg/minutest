package example.junit5

import com.oneeyedmen.minutest.junit.JUnit5Minutests
import com.oneeyedmen.minutest.rootContext
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assumptions

class AssumeInRootContext : JUnit5Minutests {
    override val tests = rootContext<Unit> {
        test("assume in a test aborts it") {
            Assumptions.assumeTrue("black".toLowerCase() == "white")
            Assertions.fail<Nothing>("shouldn't get here")
        }
    }
    
}