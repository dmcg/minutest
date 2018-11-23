package example.b

import com.oneeyedmen.minutest.experimental.context

val `this context  should not appear in the test plan` = context<Unit> {
    test("this test should not appear in the test plan") {
    
    }
}
