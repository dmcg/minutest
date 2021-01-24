package dev.minutest

import dev.minutest.junit.JUnit5Minutests
import dev.minutest.junit.experimental.JUnit4Minutests
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.platform.commons.annotation.Testable

private fun singleRoot() = rootContext {
    context("outer") {
        context("inner") {
            instrumentedTest2("test") { _, testDescriptor ->
                assertEquals("tests/outer/inner/test", testDescriptor.pathAsString())
            }
        }
    }
}

class SingleRootNamingTests5 : JUnit5Minutests {
    fun tests() = singleRoot()
}

class SingleRootNamingTests4 : JUnit4Minutests() {
    fun tests() = singleRoot()
}

class SingleRootNamingTestsX {
    @Testable
    fun tests() = singleRoot()
}

private fun overridenName() = rootContext("override name") {
    context("outer") {
        context("inner") {
            instrumentedTest2("test") { _, testDescriptor ->
                assertEquals("override name/outer/inner/test", testDescriptor.pathAsString())
            }
        }
    }
}

class SingleRootOverrideNamingTests5 : JUnit5Minutests {
    fun tests() = overridenName()
}

class SingleRootOverrideNamingTests4 : JUnit4Minutests() {
    fun tests() = overridenName()
}

class SingleRootOverrideNamingTestsX {
    @Testable
    fun tests() = overridenName()
}

private fun root1() = rootContext {
    context("outer") {
        context("inner") {
            instrumentedTest2("test") { _, testDescriptor ->
                assertEquals("tests1/outer/inner/test", testDescriptor.pathAsString())
            }
        }
    }
}

private fun root2() = rootContext("override name") {
    context("outer") {
        context("inner") {
            instrumentedTest2("test") { _, testDescriptor ->
                assertEquals("override name/outer/inner/test", testDescriptor.pathAsString())
            }
        }
    }
}

class TwoRootNamingTests5 : JUnit5Minutests {
    fun tests1() = root1()
    fun test2() = root2()
}

class TwoRootNamingTests4 : JUnit4Minutests() {
    fun tests1() = root1()
    fun tests2() = root2()
}

class TwoRootNamingTestsX() {
    @Testable
    fun tests1() = root1()
    @Testable
    fun tests2() = root2()
}