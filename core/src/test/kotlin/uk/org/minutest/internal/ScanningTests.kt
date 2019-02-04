package uk.org.minutest.internal

import org.junit.jupiter.api.Test
import uk.org.minutest.assertLogged

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

private fun uk.org.minutest.Node<*>.visit(log: MutableList<String>, indent: Int) {
    log.add(indent.spaces() + this.name)
    if (this is uk.org.minutest.Context<*, *>) {
        this.children.forEach { it.visit(log, indent + 2) }
    }

}

private fun Int.spaces() = " ".repeat(this)