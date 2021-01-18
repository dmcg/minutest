package dev.minutest

import dev.minutest.junit.JUnit5Minutests
import org.junit.jupiter.api.Assertions.assertEquals

class SingleRootNamingTests : JUnit5Minutests {

    fun name() = rootContext {
        context("outer") {
            context("inner") {
                test("test") { testDescriptor ->
                    assertEquals("name/outer/inner/test", testDescriptor.pathAsString())
                }
            }
        }
    }
}

class SingleRootOverrideNamingTests : JUnit5Minutests {

    fun name() = rootContext("override name") {
        context("outer") {
            context("inner") {
                test("test") { testDescriptor ->
                    assertEquals("override name/outer/inner/test", testDescriptor.pathAsString())
                }
            }
        }
    }
}

class TwoRootNamingTests : JUnit5Minutests {

    fun name() = rootContext {
        context("outer") {
            context("inner") {
                test("test") { testDescriptor ->
                    assertEquals("name/outer/inner/test", testDescriptor.pathAsString())
                }
            }
        }
    }

    fun name2() = rootContext("override name") {
        context("outer") {
            context("inner") {
                test("test") { testDescriptor ->
                    assertEquals("override name/outer/inner/test", testDescriptor.pathAsString())
                }
            }
        }
    }
}