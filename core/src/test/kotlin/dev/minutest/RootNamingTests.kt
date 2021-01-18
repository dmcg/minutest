package dev.minutest

import dev.minutest.junit.JUnit5Minutests
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import java.util.*

class SingleTestNamingTests : JUnit5Minutests {

    val log: MutableList<String> = Collections.synchronizedList(mutableListOf<String>())

    @AfterEach
    fun checkLog() {
        assertEquals(
            setOf(
                "names are passed to fixures and tests/outer/outer test : fixture",
                "names are passed to fixures and tests/outer/outer test : test",
                "names are passed to fixures and tests/outer/inner/inner test 1 : fixture",
                "names are passed to fixures and tests/outer/inner/inner test 1 : test",
                "names are passed to fixures and tests/outer/inner/inner test 2 : fixture",
                "names are passed to fixures and tests/outer/inner/inner test 2 : test"
            ),
            log.toSet()
        )
    }

    fun `names are passed to fixures and tests`() = rootContext {

        fixture { testDescriptor ->
            log.add(testDescriptor.pathAsString() + " : fixture")
        }

        context("outer") {
            test("outer test") { testDescriptor ->
                log.add(testDescriptor.pathAsString() + " : test")
            }

            context("inner") {
                test("inner test 1") { testDescriptor ->
                    log.add(testDescriptor.pathAsString() + " : test")
                }
                test("inner test 2") { testDescriptor ->
                    log.add(testDescriptor.pathAsString() + " : test")
                }
            }
        }
    }
}