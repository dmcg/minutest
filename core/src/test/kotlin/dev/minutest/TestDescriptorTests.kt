package dev.minutest

import dev.minutest.testing.runTests
import org.junit.jupiter.api.Test

class TestDescriptorTests {

    @Test
    fun `names are passed to fixtures and tests`() {

        val log = mutableListOf<String>().synchronized()

        runTests(
            rootContext {

                beforeAll { testDescriptor ->
                    log.add(testDescriptor.pathAsString() + " : beforeAll")
                }

                fixture { testDescriptor ->
                    log.add(testDescriptor.pathAsString() + " : fixture")
                }

                instrumentedBeforeEach { _, testDescriptor ->
                    log.add(testDescriptor.pathAsString() + " : before")
                }

                context("outer") {
                    instrumentedTest2("outer test") { _, testDescriptor ->
                        log.add(testDescriptor.pathAsString() + " : test")
                    }

                    context("inner") {
                        beforeAll { testDescriptor ->
                            log.add(testDescriptor.pathAsString() + " : beforeAll")
                        }
                        instrumentedTest2("inner test 1") { _, testDescriptor ->
                            log.add(testDescriptor.pathAsString() + " : test")
                        }
                        instrumentedTest2("inner test 2") { _, testDescriptor ->
                            log.add(testDescriptor.pathAsString() + " : test")
                        }
                        afterAll { testDescriptor ->
                            log.add(testDescriptor.pathAsString() + " : afterAll")
                        }
                    }
                }

                after { testDescriptor ->
                    log.add(testDescriptor.pathAsString() + " : after")
                }

                afterAll { testDescriptor ->
                    log.add(testDescriptor.pathAsString() + " : afterAll")
                }
            }
        ).withNoExceptions()

        assertLoggedInAnyOrder(
            log,
            "root : afterAll",
            "root/outer/outer test : fixture",
            "root/outer/outer test : before",
            "root/outer/outer test : after",
            "root/outer/outer test : test",
            "root/outer/inner : beforeAll",
            "root/outer/inner/inner test 1 : fixture",
            "root/outer/inner/inner test 1 : test",
            "root/outer/inner/inner test 1 : before",
            "root/outer/inner/inner test 1 : after",
            "root/outer/inner/inner test 2 : fixture",
            "root/outer/inner/inner test 2 : test",
            "root/outer/inner/inner test 2 : before",
            "root/outer/inner/inner test 2 : after",
            "root/outer/inner : afterAll",
            "root : beforeAll",
        )

    }
}