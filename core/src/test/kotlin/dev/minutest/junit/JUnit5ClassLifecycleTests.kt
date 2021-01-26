package dev.minutest.junit

import dev.minutest.assertLoggedInAnyOrder
import dev.minutest.rootContext
import dev.minutest.synchronized
import dev.minutest.test
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach

private val log = mutableListOf<String>().synchronized()

/**
 * Just to remind me what is run and when.
 */
class JUnit5ClassLifecycleTests : JUnit5Minutests {

    companion object {
        @BeforeAll @JvmStatic fun beforeAll() {
            log.add("beforeAll")
        }

        @AfterAll @JvmStatic fun afterAll() {
            log.add("afterAll")
            assertLoggedInAnyOrder(log, "beforeAll", "init", "beforeEach", "1", "2", "afterEach", "afterAll")
        }
    }

    init {
        log.add("init")
    }

    @BeforeEach fun beforeEach() {
        log.add("beforeEach")
    }

    @AfterEach fun afterEach() {
        log.add("afterEach")
    }

    fun tests1() = rootContext {
        test("1") {
            log.add("1")
        }
    }

    fun tests2() = rootContext {
        test("2") {
            log.add("2")
        }
    }
}