package dev.minutest.junit.engine

import dev.minutest.rootContext
import dev.minutest.test
import org.junit.platform.commons.annotation.Testable

@Testable
fun `root name`() = rootContext("context name") {
    test("hello") {
// Uncomment to check the test is being run for now
//        org.junit.Assert.fail("")
    }
}