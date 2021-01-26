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
                test("there needs to be a test") {}
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
                test("there needs to be a test") {}
            }.buildNode()
        }
    }

    @Test
    fun `throws IllegalStateException if fixture is specified twice in a context`() {
        assertThrows<IllegalStateException> {
            rootContext<String> {
                given { "banana" }
                given { "banana" }
            }.buildNode()
        }
    }

    @Test
    fun `throws IllegalStateException if a sub-context does not provide a fixture`() {
        assertThrows<IllegalStateException> {
            rootContext<String> {
                context("subcontext") {
                    test("there needs to be a test") {}
                }
            }.buildNode()
        }
    }

    @Test
    fun `throws IllegalStateException if parent fixture is not compatible and no deriveFixture specified`() {
        assertThrows<IllegalStateException> {
            rootContext<CharSequence> {
                given { "banana" }
                derivedContext<String>("subcontext") {
                    test("there needs to be a test") {}
                }
            }.buildNode()
        }
    }

    @Test
    fun `throws IllegalStateException if parent fixture is not nullably compatible and no deriveFixture specified`() {
        assertThrows<IllegalStateException> {
            rootContext<String?> {
                given { null }
                derivedContext<String>("subcontext") {
                    test("there needs to be a test") {
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
                    given_ { parentFixture -> parentFixture + "banana" } // this won't have been supplied, so we should forbid
                    test("test") {
                    }
                }
            }.buildNode()
        }

        // It should be OK for Unit though
        rootContext<Unit> {
            context("parent had no fixture") {
                given_ {
                }
                test("test") {
                }
            }
        }.buildNode()
    }
}