package com.oneeyedmen.minutest

import com.oneeyedmen.minutest.junit.junitTests
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.TestFactory


class AfterAllTests {

    // this is a very special case for testing testing - don't do this normally
    val log = mutableListOf<String>()
    private lateinit var expectedLog: List<String>

    @AfterEach fun checkLog() {
        assertEquals(expectedLog, log)
    }

    @TestFactory fun `top level`() = junitTests<MutableList<String>> {

        expectedLog = listOf("test 1", "test 2", "after all")

        fixture { log }

        afterAll {
            log.add("after all")
        }

        test("test 1") {
            add("test 1")
        }

        test("test 2") {
            add("test 2")
        }
    }

    @TestFactory fun `not top level`() = junitTests<MutableList<String>> {

        expectedLog = listOf("test 1", "test 2", "after all")

        fixture { log }

        context("inside") {

            afterAll {
                log.add("after all")
            }

            test("test 1") {
                add("test 1")
            }

            test("test 2") {
                add("test 2")
            }
        }
    }
}

