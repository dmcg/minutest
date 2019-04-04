package dev.minutest.experimental

import dev.minutest.Node
import dev.minutest.Test
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import kotlin.test.assertEquals
import kotlin.test.fail
import org.junit.jupiter.api.Test as JUnitTest

class AnnotationTortureTests : JUnit5Minutests {

    fun numberTests() = rootContext<Number> {

        fixture { 42.0 }

        numberAnnotation - test("test") {
            assertEquals(43.0, this)
        }

        anyAnnotation - test("test") {
            assertEquals(42.0, this)
        }

        // Doesn't compile
        // doubleAnnotation - test("test") {
        //     assertEquals(43.0, this)
        // }
    }

    fun floatTests() = rootContext<Double> {

        fixture { 42.0 }

        numberAnnotation - test("test") {
            assertEquals(43.0, this)
        }

        anyAnnotation - test("test") {
            assertEquals(42.0, this)
        }

        doubleAnnotation - test("test") {
            assertEquals(43.0, this)
        }

        doubleAnnotation + numberAnnotation - test("test") {
            assertEquals(44.0, this)
        }

        numberAnnotation + doubleAnnotation - test("test") {
            assertEquals(44.0, this)
        }

        SKIP + doubleAnnotation - test("test") {
            fail()
        }

        doubleAnnotation + SKIP - test("test") {
            fail()
        }
    }

    fun smokingGun() = rootContext<Double> {
        fixture { 42.0 }

        SKIP + badAnyAnnotation - test("will throw java.lang.ClassCastException: kotlin.Unit cannot be cast to java.lang.Number") {}
    }
}

private val numberAnnotation = TransformingAnnotation() { node: Node<Number> ->
    when (node) {
        is Test -> node.copy(name = "numberAnnotation + ${node.name}").copy { fixture, testDescriptor ->
            node(fixture.toDouble() + 1, testDescriptor)
        }
        else -> error("only handling tests here")
    }
}

private val doubleAnnotation = TransformingAnnotation() { node: Node<Double> ->
    when (node) {
        is Test -> node.copy(name = "doubleAnnotation + ${node.name}").copy { fixture, testDescriptor ->
            node(fixture + 1, testDescriptor)
        }
        else -> error("only handling tests here")
    }
}

private val anyAnnotation = TransformingAnnotation() { node: Node<Any> ->
    when (node) {
        is Test -> node.copy(name = "anyAnnotation + ${node.name}").copy { fixture, testDescriptor ->
            node(fixture, testDescriptor)
        }
        else -> error("only handling tests here")
    }
}

private val badAnyAnnotation = TransformingAnnotation() { node: Node<Any> ->
    when (node) {
        is Test -> node.copy { fixture, testDescriptor ->
            node(Unit, testDescriptor)
        }
        else -> error("only handling tests here")
    }
}
