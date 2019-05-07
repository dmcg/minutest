package dev.minutest.experimental

import dev.minutest.assertLogged
import dev.minutest.executeTests
import dev.minutest.rootContext
import org.junit.jupiter.api.Test as JUnitTest


class AutoCloseTests {

    val log = mutableListOf<String>()

    @JUnitTest fun test() {
        val tests = rootContext {

            val resource by autoClose {
                log.add("resource created")
                object : AutoCloseable {
                    override fun close() {
                        log.add("resource closed")
                    }

                    override fun toString() = "resource accessed"
                }
            }

            test("test 1") {
                log.add("test 1")
                log.add(resource.toString())
            }

            test("test 2") {
                log.add("test 2")
                log.add(resource.toString())
            }
        }

        executeTests(tests)
        assertLogged(log,
            "test 1", "resource created", "resource accessed",
            "test 2", "resource accessed",
            "resource closed")
    }

    @JUnitTest fun `doesnt close if resource not accessed`() {
        val tests = rootContext {

            @Suppress("UNUSED_VARIABLE")
            val resource by autoClose {
                log.add("resource created")
                object : AutoCloseable {
                    override fun close() {
                        log.add("resource closed")
                    }

                    override fun toString() = "resource accessed"
                }
            }

            test("test 1") {
                log.add("test 1")
            }

        }

        executeTests(tests)
        assertLogged(log, "test 1")
    }
}