package dev.minutest

import dev.minutest.junit.toTestFactory
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.fail
import java.io.FileNotFoundException


class FixtureNotSuppliedTests {

    @Test
    fun `throws IllegalStateException if no fixture specified when one is needed by a test`() {
        assertThrows<IllegalStateException> {
            rootContext<String> {
                test("there needs to be a test") {}
            }.toTestFactory()
        }
    }

    @Test
    fun `throws IllegalStateException if no fixture specified when one is needed by a fixture`() {
        assertThrows<IllegalStateException> {
            rootContext<String> {
                modifyFixture {
                    fixture
                }
                test("there needs to be a test") {}
            }.toTestFactory()
        }
    }

    @Test
    fun `throws IllegalStateException if fixture is specified twice in a context`() {
        assertThrows<IllegalStateException> {
            rootContext<String> {
                fixture { "banana" }
                fixture { "banana" }
            }.toTestFactory()
        }
    }

    @Test
    fun `throws IllegalStateException if a sub-context does not provide a fixture`() {
        assertThrows<IllegalStateException> {
            rootContext<String> {
                context("subcontext") {
                    test("there needs to be a test") {}
                }
            }.toTestFactory()
        }
    }

    @Test
    fun `throws IllegalStateException if parent fixture is not compatible and no deriveFixture specified`() {
        assertThrows<IllegalStateException> {
            rootContext<CharSequence> {
                fixture { "banana" }
                derivedContext<String>("subcontext") {
                    test("there needs to be a test") {}
                }
            }.toTestFactory()
        }
    }

    @Test
    fun `throws IllegalStateException if parent fixture is not nullably compatible and no deriveFixture specified`() {
        assertThrows<IllegalStateException> {
            rootContext<String?> {
                fixture { null }
                derivedContext<String>("subcontext") {
                    test("there needs to be a test") {
                        @Suppress("SENSELESS_COMPARISON") // except it isn't because it will be
                        if (this != null) fail("")
                    }
                }
            }.toTestFactory()
        }
    }

    @Test
    fun `throws exception thrown from fixture during execution`() {
        val tests = rootContext<String> {
            fixture {
                throw FileNotFoundException()
            }
            test("there needs to be a test to run anything") {}

        }
        checkItems(executeTests(tests), { it is FileNotFoundException })
    }

    // TODO - a bug
    @Test
    fun `throws IllegalStateException if you deriveFixture in a child when parent had punted`() {
        assertThrows<IllegalStateException> {
            rootContext<String> {
                context("parent had no fixture") {
                    deriveFixture { this + "banana" } // this won't have been supplied, so we should forbid
                    test("test") {
                    }
                }
            }.toTestFactory()
        }

        // It should be OK for Unit though
        rootContext<Unit> {
            context("parent had no fixture") {
                deriveFixture { this }
                test("test") {
                }
            }
        }
    }
}