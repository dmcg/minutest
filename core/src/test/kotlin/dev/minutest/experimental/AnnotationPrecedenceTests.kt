package dev.minutest.experimental

import dev.minutest.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test as JUnitTest


class AnnotationPrecedenceTests {

    val log = mutableListOf<String>()

    @JUnitTest fun `first test transform is outer`() {

        executeTests(
            rootContext<Unit> {
                Annotation("1") + Annotation("2") - test("test") {}
            }
        )
        assertLogged(log,
            "Enter 1",
            "Enter 2",
            "Exit 2",
            "Exit 1"
        )
    }

    @JUnitTest fun `first context transform is outer`() {

        executeTests(
            Annotation("1") + Annotation("2") - rootContext<Unit> {
                test("test") {}
            }
        )
        assertLogged(log,
            "Enter 1",
            "Enter 2",
            "Exit 2",
            "Exit 1"
        )
    }

    @Disabled("TODO")
    @JUnitTest fun `first internal transform is outer`() {

        executeTests(
            rootContext<Unit> {
                annotateWith(Annotation("1"))
                annotateWith(Annotation("2"))
                test("test") {}
            }
        )
        assertLogged(log,
            "Enter 1",
            "Enter 2",
            "Exit 2",
            "Exit 1"
        )
    }

    @JUnitTest fun `external annotations are outside internal`() {

        executeTests(
            rootContext<Unit> {
                Annotation("1") - context("inner") {
                    annotateWith(Annotation("2"))
                    test("test") {}
                }
            }
        )
        // this is because the builder block is invoked as part of the context call, and only then can minus be applied
        assertLogged(log,
            "Enter 1",
            "Enter 2",
            "Exit 2",
            "Exit 1"
        )
    }

    @Disabled("TODO")
    @JUnitTest fun `external annotations are outside internal for rootContext`() {

        executeTests(
            Annotation("1") - rootContext<Unit> {
                annotateWith(Annotation("2"))
                test("test") {}
            }
        )
        // rootContext is consistent with a nested context
        assertLogged(log,
            "Enter 1",
            "Enter 2",
            "Exit 2",
            "Exit 1"
        )
    }

    @Suppress("UNCHECKED_CAST") // a bit suspicious but just a test
    inner class Annotation(private val marker: String) : TransformingAnnotation<Unit>({ node: Node<Unit> ->
        when (node) {
            is Test<Unit> -> node.copy { fixture, testDescriptor ->
                log.add("Enter $marker")
                try {
                    node.invoke(fixture, testDescriptor)
                } finally {
                    log.add("Exit $marker")
                }
            }
            is Context<Unit, *> -> ContextWrapper(
                delegate = node as Context<Unit, Unit>,
                runner = { test: Testlet<Unit>, parentFixture: Unit, testDescriptor: TestDescriptor ->
                    log.add("Enter $marker")
                    try {
                        node.runTest(test, parentFixture, testDescriptor)
                    } finally {
                        log.add("Exit $marker")
                    }
                }
            )
        }
    })
}

