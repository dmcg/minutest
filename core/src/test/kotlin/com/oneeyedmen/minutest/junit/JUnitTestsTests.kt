package com.oneeyedmen.minutest.junit

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import java.util.*


object JUnitTestsTests : JUnitTests<String>({

    fixture { "banana" }

    test("test") {
        assertEquals("banana", this)
    }
})

object JUnitTestsTests2 : JUnitTests<Stack<String>>({

    fixture { Stack() }

    test("test") {
        assertTrue(this.isEmpty())
    }
})

