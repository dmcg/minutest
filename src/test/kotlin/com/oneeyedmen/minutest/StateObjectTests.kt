package com.oneeyedmen.minutest

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicContainer.dynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory


class System {
    var thing = "banana"
}

object StateObjectTests {

    @TestFactory fun fred() = listOf(
        context<System>("context") {
            subject { System() }
            test("can mutate without affecting following tests") {
                thing = "kumquat"
                assertEquals("kumquat", thing)
            }
            test("previous test did not affect me") {
                assertEquals("banana", thing)
            }
        }
    )
}

class TestContext<T> {
    private var subjectBuilder: (() -> T)? = null
    private val tests = mutableListOf<Pair<String, T.() -> Any>>()

    fun subject(f: () -> T) {
        subjectBuilder = f
    }

    fun test(name: String, f: T.() -> Any) = tests.add(name to f)

    fun build(name: String): DynamicContainer = dynamicContainer(name,
        tests.map { test ->
            dynamicTest(test.first) {
                test.second(subjectBuilder!!())
            }
        }
    )

}

fun <T> context(name: String, f: TestContext<T>.() -> Any): DynamicNode =
    TestContext<T>().apply {
        f()
    }.build(name)