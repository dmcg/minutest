package dev.minutest.experimental

import dev.minutest.*
import dev.minutest.testing.runTests
import org.junit.jupiter.api.Test as JUnitTest


class AnnotationPrecedenceTests {

    val log = mutableListOf<String>()

    @JUnitTest
    fun `first test transform is outer`() {

        runTests(
            rootContext {
                transform("1") + transform("2") - test2("test") {}
            }
        )
        assertLogged(log,
            "Enter 1",
            "Enter 2",
            "Exit 2",
            "Exit 1"
        )
    }

    @JUnitTest
    fun `first context transform is outer`() {

        runTests(
            rootContext {
                transform("1") + transform("2") - context("inner") {
                    test2("test") {}
                }
            }
        )
        assertLogged(log,
            "Enter 1",
            "Enter 2",
            "Exit 2",
            "Exit 1"
        )
    }

    @JUnitTest
    fun `first root context transform is outer`() {

        runTests(
            transform("1") + transform("2") - rootContext {
                test2("test") {}
            }
        )
        assertLogged(log,
            "Enter 1",
            "Enter 2",
            "Exit 2",
            "Exit 1"
        )
    }

    @JUnitTest
    fun `first internal transform is outer`() {

        runTests(
            rootContext {
                annotateWith(transform("1"))
                annotateWith(transform("2"))
                test2("test") {}
            }
        )
        assertLogged(log,
            "Enter 1",
            "Enter 2",
            "Exit 2",
            "Exit 1"
        )
    }

    @JUnitTest
    fun `external annotations are outside internal`() {

        runTests(
            rootContext {
                transform("1") - context("inner") {
                    annotateWith(transform("2"))
                    test2("test") {}
                }
            }
        )
        assertLogged(log,
            "Enter 1",
            "Enter 2",
            "Exit 2",
            "Exit 1"
        )
    }

    @JUnitTest
    fun `external annotations are outside internal for rootContext`() {

        runTests(
            transform("1") - rootContext {
                annotateWith(transform("2"))
                test2("test") {}
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

    private fun transform(marker: String) = Annotation( { log.add(it) }, marker)
}

private class Annotation(
    private val log: (String) -> Unit,
    private val marker: String
) : TransformingAnnotation() {
    override fun <F> transform(node: Node<F>): Node<F> =
        when (node) {
            is Test<F> -> node.loggedTo(log, marker)
            is Context<F, *> -> node.loggedTo(log, marker)
        }

}

private fun <F> Test<F>.loggedTo(log: (String) -> Unit, marker: String) = copy { fixture, testDescriptor ->
    log("Enter $marker")
    try {
        invoke(fixture, testDescriptor)
    } finally {
        log("Exit $marker")
    }
}

private fun <PF, F> Context<PF, F>.loggedTo(log: (String) -> Unit, marker: String): Context<PF, F> = ContextWrapper(
    delegate = this,
    runner = { test: Testlet<F>, parentFixture: PF, testDescriptor: TestDescriptor ->
        log("Enter $marker")
        try {
            this.runTest(test, parentFixture, testDescriptor)
        } finally {
            log("Exit $marker")
        }
    }
)

