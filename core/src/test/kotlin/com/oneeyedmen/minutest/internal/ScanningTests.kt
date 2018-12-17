package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.RuntimeNode
import com.oneeyedmen.minutest.RuntimeTest
import com.oneeyedmen.minutest.fullName
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ScanningTests {
    @Test
    fun `scanned tests have correct full names`() {
        val scan = scan({whitelistPackages("example.a")})
        
        val tests = scan.asSequence().tests().map { it.fullName().joinToString("/") }.toSet()
        assertEquals(
            setOf(
                "example.a/example context/a failing test",
                "example.a/example context/a passing test",
                "example.a/example skipped context/skipping example skipped context",
                "example.a/example typed context/a typed fixture test"
            ),
            tests
        )
    }
}

private fun Sequence<RuntimeNode<*, *>>.tests(): Sequence<RuntimeTest<*>> = flatMap { it.tests() }

private fun RuntimeNode<*, *>.tests(): Sequence<RuntimeTest<*>> = when (this) {
    is RuntimeTest -> sequenceOf(this)
    is RuntimeContext -> children.asSequence().flatMap { it.tests() }
}
