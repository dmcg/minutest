package samples.minutestRunner.b

import dev.minutest.rootContext
import dev.minutest.test
import org.junit.platform.commons.annotation.Testable

@Testable
val `this context  should not appear in the test plan` = rootContext {
    test("this test should not appear in the test plan") {
    
    }
}
