package dev.minutest

import dev.minutest.junit.JUnit5Minutests
import dev.minutest.junit.experimental.JUnit4Minutests
import org.junit.AfterClass
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.platform.commons.annotation.Testable

open class Checking {
    val classLog = mutableListOf<String>().synchronized()
    fun log(s: String) {
        classLog.add(s)
    }
    fun checkLog() {
        assertEquals(
            setOf(
                "rootTests : beforeAll",
                "rootTests/required for JUnit not to skip : test",
                "rootTests : afterAll"
            ),
            classLog.toSet()
        )
    }
}

private fun contract(log: (s: String) -> Unit) =
    rootContext("rootTests") {
        beforeAll { testDescriptor ->
            log(testDescriptor.pathAsString() + " : beforeAll")
        }
        test("required for JUnit not to skip") { testDescriptor ->
            log(testDescriptor.pathAsString() + " : test")
        }
        afterAll { testDescriptor ->
            log(testDescriptor.pathAsString() + " : afterAll")
        }
    }

class BeforeAndAfterRootTests5 : JUnit5Minutests {
    companion object : Checking() {
        @JvmStatic
        @AfterAll
        fun check() {
            checkLog()
        }
    }
    fun tests() = contract { log(it) }
}

class BeforeAndAfterRootTests4 : JUnit4Minutests() {
    companion object : Checking() {
        @JvmStatic
        @AfterClass
        fun check() {
            checkLog()
        }
    }
    fun tests() = contract { log(it) }
}

// I can't find a sensible way to get an automated check of this
class BeforeAndAfterRootTestsX {
    fun log(s: String) {
        // Commented out because otherwise IntelliJ's test output
        // shows every one three times (I think its confused with
        // parallel test runs).
        // println(s)
    }

    @Testable
    fun tests() = contract(::log)
}