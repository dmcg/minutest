package dev.minutest.junit.experimental

import dev.minutest.rootContext


fun `root name`() = rootContext("context name") {
    test("hello") {
// Uncomment to check the test is being run for now
//        org.junit.Assert.fail("")
    }
}