package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.RuntimeNode
import com.oneeyedmen.minutest.assertLogged
import org.junit.jupiter.api.Test

class ScanningTests {

    @Test
    fun `scanned tests have correct full names`() {
        val scan: List<ScannedPackageContext> = scan({ whitelistPackages("samples.minutestRunner.a") })

        val log = mutableListOf<String>()
        scan.forEach {
            it.visit(log, 0)
        }

        assertLogged(log,
            "samples.minutestRunner.a",
            "  example context",
            "    a failing test",
            "    a passing test",
            "  example skipped context",
            "  example typed context",
            "    a typed fixture test"
        )
    }
}

private fun RuntimeNode<*>.visit(log: MutableList<String>, indent: Int) {
    log.add(indent.spaces() + this.name)
    if (this is RuntimeContext<*, *>) {
        this.children.forEach { it.visit(log, indent + 2) }
    }

}

private fun Int.spaces() = " ".repeat(this)