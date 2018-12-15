package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.RuntimeNode
import com.oneeyedmen.minutest.RuntimeTest
import com.oneeyedmen.minutest.fullName
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ScanningTests {
    @Test
    @Disabled("bug report")
    fun `scanned tests have correct full names`() {
        val scan = scan({whitelistPackages("example.a")})
        
        val tests = scan.asSequence().tests().map { it.fullName().joinToString("/") }.toSet()
        assertEquals(
            setOf(
                "example.a/a failing test",
                "example.a/a passing test",
                "example.a/example skipped context",
                "example.a/a typed fixture test"
            ),
            tests
        )
    }
}

private fun Sequence<RuntimeNode>.tests(): Sequence<RuntimeTest> = flatMap { it.tests() }

private fun RuntimeNode.tests(): Sequence<RuntimeTest> = when (this) {
    is RuntimeTest -> sequenceOf(this)
    is RuntimeContext<*> -> children.asSequence().flatMap { it.tests() }
}
