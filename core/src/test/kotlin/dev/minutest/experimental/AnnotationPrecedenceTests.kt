package dev.minutest.experimental

import dev.minutest.*
import org.junit.jupiter.api.Test as JUnitTest


class AnnotationPrecedenceTests {

    val log = mutableListOf<String>()

    @JUnitTest fun `leftmost transform is innermost`() {

        executeTests(
            rootContext<Unit> {
                Annotation("1") + Annotation("2") - test("test") {}
            }
        )
        assertLogged(log,
            "Enter 2",
            "Enter 1",
            "Exit 1",
            "Exit 2")
    }

    @JUnitTest fun `preamble annotations are merged in order`() {

        executeTests(
            Annotation("1") + Annotation("2") - rootContext<Unit> {
                test("test") {}
            }
        )
        assertLogged(log,
            "Enter 2",
            "Enter 1",
            "Exit 1",
            "Exit 2")
    }

    @JUnitTest fun `internal annotations are merged in order`() {

        executeTests(
            rootContext<Unit> {
                annotateWith(Annotation("1"))
                annotateWith(Annotation("2"))
                test("test") {}
            }
        )
        assertLogged(log,
            "Enter 2",
            "Enter 1",
            "Exit 1",
            "Exit 2")
    }

    @JUnitTest fun `internal annotations take precedence`() {

        executeTests(
            rootContext<Unit> {
                Annotation("2") - context("inner") {
                    annotateWith(Annotation("1"))
                    test("test") {}
                }
            }
        )
        // this is because the builder block is invoked as part of the context call, and only then can minus be applied
        assertLogged(log,
            "Enter 2",
            "Enter 1",
            "Exit 1",
            "Exit 2")
    }

    @JUnitTest fun `internal annotations take precedence for rootContext`() {

        executeTests(
            Annotation("2") - rootContext<Unit> {
                annotateWith(Annotation("1"))
                test("test") {}
            }
        )
        // rootContext is consistent with a nested context
        assertLogged(log,
            "Enter 2",
            "Enter 1",
            "Exit 1",
            "Exit 2")
    }

    @Suppress("UNCHECKED_CAST")
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

