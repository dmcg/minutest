package dev.minutest

import dev.minutest.junit.JUnit5Minutests
import dev.minutest.junit.MinutestJUnit4Runner
import org.junit.AfterClass
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.platform.commons.annotation.Testable
import org.junit.runner.RunWith
import java.util.Collections.synchronizedList

open class Checking {
    val classLog = mutableListOf<String>().synchronized()
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

abstract class BeforeAndAfterAllRootTests {
    abstract fun log(s: String)

    open fun rootTests() = rootContext {
        beforeAll { testDescriptor ->
            log(testDescriptor.pathAsString() + " : beforeAll")
        }
        test("required for JUnit not to skip") { testDescriptor ->
            log(testDescriptor.pathAsString() + " : test")
        }
        afterAll { testDescriptor ->
            log(testDescriptor.pathAsString()  + " : afterAll")
        }
    }
}

class BeforeAndAfterRootTests5 : BeforeAndAfterAllRootTests(), JUnit5Minutests {
    companion object : Checking() {
        @JvmStatic
        @AfterAll
        fun check() {
            checkLog()
        }
    }
    override fun log(s: String) {
        classLog.add(s)
    }
}

@RunWith(MinutestJUnit4Runner::class)
class BeforeAndAfterRootTests4 : BeforeAndAfterAllRootTests() {
    companion object : Checking() {
        @JvmStatic
        @AfterClass
        fun check() {
            checkLog()
        }
    }
    override fun log(s: String) {
        classLog.add(s)
    }
}

// I can't find a way to get an automated check of this
class BeforeAndAfterRootTestsX: BeforeAndAfterAllRootTests() {
    override fun log(s: String) {
        println(s)
    }

    @Testable
    override fun rootTests() = super.rootTests()
}

@Suppress("UNCHECKED_CAST")
fun <T, L: List<T>> L.synchronized(): L = synchronizedList(this) as L

