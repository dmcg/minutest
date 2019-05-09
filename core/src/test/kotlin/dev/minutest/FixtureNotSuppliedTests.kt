package dev.minutest

import dev.minutest.junit.toTestFactory
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.FileNotFoundException


class FixtureNotSuppliedTests {

    @Test fun `throws IllegalStateException if no fixture specified when one is needed by a test`() {
        assertThrows<IllegalStateException> {
            rootContext<String> {
                test("there needs to be a test") {}
            }.toTestFactory()
        }
    }

    @Test fun `throws IllegalStateException if no fixture specified when one is needed by a fixture`() {
        assertThrows<IllegalStateException> {
            rootContext<String> {
                modifyFixture {
                    fixture
                }
                test("there needs to be a test") {}
            }.toTestFactory()
        }
    }

    @Test fun `throws IllegalStateException if fixture is specified twice in a context`() {
        assertThrows<IllegalStateException> {
            rootContext<String> {
                fixture { "banana" }
                fixture { "banana" }
            }.toTestFactory()
        }
    }

    @Test fun `throws IllegalStateException if a sub-context does not provide a fixture`() {
        assertThrows<IllegalStateException> {
            rootContext<String> {
                context("subcontext") {
                    test("there needs to be a test") {}
                }
            }.toTestFactory()
        }
    }

    @Test fun `throws exception thrown from fixture during execution`() {
        val tests = rootContext<String> {
            fixture {
                throw FileNotFoundException()
            }
            test("there needs to be a test to run anything") {}

        }
        checkItems(executeTests(tests), { it is FileNotFoundException })
    }
}