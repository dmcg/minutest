package dev.minutest.junit

import dev.minutest.assertLogged
import dev.minutest.rootContext
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach

private val log = mutableListOf<String>()
private var hashCode: Int = 0

/**
 * Just to remind me what is run and when.
 */
object JUnit5ObjectLifecycleTests : JUnit5Minutests {

    @BeforeAll @JvmStatic fun beforeAll() {
        log.add("beforeAll")
    }

    @AfterAll @JvmStatic fun afterAll() {
        log.add("afterAll")
        assertLogged(log, "init", "beforeAll", "beforeEach", "1", "2", "afterEach", "afterAll")
    }

    init {
        hashCode = System.identityHashCode(this@JUnit5ObjectLifecycleTests)
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