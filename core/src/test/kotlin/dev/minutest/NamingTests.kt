package dev.minutest

import dev.minutest.junit.JUnit5Minutests
import dev.minutest.junit.MinutestJUnit4Runner
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.platform.commons.annotation.Testable
import org.junit.runner.RunWith

abstract class SingleRootNamingTests {

    open fun name() = rootContext {
        context("outer") {
            context("inner") {
                test("test") { testDescriptor ->
                    assertEquals("name/outer/inner/test", testDescriptor.pathAsString())
                }
            }
        }
    }
}

class SingleRootNamingTests5 : SingleRootNamingTests(), JUnit5Minutests

@RunWith(MinutestJUnit4Runner::class)
class SingleRootNamingTests4 : SingleRootNamingTests()

class SingleRootNamingTestsX: SingleRootNamingTests() {
    @Testable
    override fun name() = super.name()
}

abstract class SingleRootOverrideNamingTests {

    open fun name() = rootContext("override name") {
        context("outer") {
            context("inner") {
                test("test") { testDescriptor ->
                    assertEquals("override name/outer/inner/test", testDescriptor.pathAsString())
                }
            }
        }
    }
}

class SingleRootOverrideNamingTests5 : SingleRootOverrideNamingTests(), JUnit5Minutests

@RunWith(MinutestJUnit4Runner::class)
class SingleRootOverrideNamingTests4 : SingleRootNamingTests()

class SingleRootOverrideNamingTestsX: SingleRootOverrideNamingTests() {
    @Testable
    override fun name() = super.name()
}

abstract class TwoRootNamingTests {

    open fun name() = rootContext {
        context("outer") {
            context("inner") {
                test("test") { testDescriptor ->
                    assertEquals("name/outer/inner/test", testDescriptor.pathAsString())
                }
            }
        }
    }

    open fun name2() = rootContext("override name") {
        context("outer") {
            context("inner") {
                test("test") { testDescriptor ->
                    assertEquals("override name/outer/inner/test", testDescriptor.pathAsString())
                }
            }
        }
    }
}

class TwoRootNamingTests5 : TwoRootNamingTests(), JUnit5Minutests

@RunWith(MinutestJUnit4Runner::class)
class TwoRootNamingTests4 : TwoRootNamingTests()

class TwoRootNamingTestsX: TwoRootNamingTests() {
    @Testable
    override fun name() = super.name()
    @Testable
    override fun name2() = super.name2()
}