package samples.minutestRunner.b

import dev.minutest.rootContext
import dev.minutest.test2
import org.junit.platform.commons.annotation.Testable

@Testable
val `this context  should not appear in the test plan` = rootContext {
    test2("this test should not appear in the test plan") {
    
    }
}
