package com.oneeyedmen.minutest.junit

import org.junit.jupiter.api.Assertions.assertTrue
import java.util.*


object JUnitTestsTests : JUnitTests<Stack<String>>({

    fixture { Stack() }

    test("is empty") {
        assertTrue(this.isEmpty())
    }
})

