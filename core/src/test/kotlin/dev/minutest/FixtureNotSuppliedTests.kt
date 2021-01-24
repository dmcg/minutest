package dev.minutest

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.fail

/**
 * Tests that we detect issues with fixture presence and type while building the context tree.
 */
class FixtureNotSuppliedTests {

    @Test
    fun `throws IllegalStateException if no fixture specified when one is needed by a test`() {
        assertThrows<IllegalStateException> {
            rootContext<String> {
                test2("there needs to be a test") {}
            }.buildNode()
        }
    }

    @Test
    fun `throws IllegalStateException if no fixture specified when one is needed by a fixture`() {
        assertThrows<IllegalStateException> {
            rootContext<String> {
                beforeEach {
                    it
                }
                test2("there needs to be a test") {}
            }.buildNode()
        }
    }

    @Test
    fun `throws IllegalStateException if fixture is specified twice in a context`() {
        assertThrows<IllegalStateException> {
            rootContext<String> {
                fixture { "banana" }
                fixture { "banana" }
            }.buildNode()
        }
    }

    @Test
    fun `throws IllegalStateException if a sub-context does not provide a fixture`() {
        assertThrows<IllegalStateException> {
            rootContext<String> {
                context("subcontext") {
                    test2("there needs to be a test") {}
                }
            }.buildNode()
        }
    }

    @Test
    fun `throws IllegalStateException if parent fixture is not compatible and no deriveFixture specified`() {
        assertThrows<IllegalStateException> {
            rootContext<CharSequence> {
                fixture { "banana" }
                derivedContext<String>("subcontext") {
                    test2("there needs to be a test") {}
                }
            }.buildNode()
        }
    }

    @Test
    fun `throws IllegalStateException if parent fixture is not nullably compatible and no deriveFixture specified`() {
        assertThrows<IllegalStateException> {
            rootContext<String?> {
                fixture { null }
                derivedContext<String>("subcontext") {
                    test2("there needs to be a test") {
                        @Suppress("SENSELESS_COMPARISON") // except it isn't because it will be
                        if (this != null) fail("")
                    }
                }
            }.buildNode()
        }
    }

    @Test
    fun `throws IllegalStateException if you deriveFixture in a child when parent had punted`() {
        assertThrows<IllegalStateException> {
            rootContext<String> {
                context("parent had no fixture") {
                    deriveFixture { this + "banana" } // this won't have been supplied, so we should forbid
                    test2("test") {
                    }
                }
            }.buildNode()
        }

        // It should be OK for Unit though
        rootContext<Unit> {
            context("parent had no fixture") {
                deriveFixture {
                }
                test2("test") {
                }
            }
        }.buildNode()
    }
}