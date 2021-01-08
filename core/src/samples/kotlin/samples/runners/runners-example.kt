package samples.runners

import dev.minutest.ContextBuilder
import dev.minutest.experimental.SKIP
import dev.minutest.experimental.minus
import kotlin.test.fail

fun ContextBuilder<Unit>.runnersExample() {

    test("test in root") {}

    test("failing test in root") {
        throw RuntimeException("fail in root")
    }

    context("inner") {
        test("test in inner") {}
        test("failing test in inner") {
            throw RuntimeException("fail in inner")
        }
    }

    SKIP - context("skipped context") {
        test("SHOULD NOT BE SEEN") {
            fail("test from fun with parameter discovered")
        }
    }

    context("context with skipped test") {
        SKIP - test("SHOULD NOT BE SEEN") {
            fail("test from fun with parameter discovered")
        }
    }
}

fun expectedRunnersLog(
    engineName: String,
    testName: String,
    rootName: String,
    abortRatherThanSkip: Boolean = true
): List<String> =
    expected.map {
        it
            .replace("ENGINE_NAME", engineName)
            .replace("TEST_NAME", testName)
            .replace("ROOT_NAME", rootName)
    }.fixSkipped(abortRatherThanSkip)

private fun List<String>.fixSkipped(abortRatherThanSkip: Boolean): List<String> =
    when (abortRatherThanSkip) {
        false -> this
        else ->
            this.fold(mutableListOf<String>()) { acc, line ->
                acc.apply {
                    if (line.startsWith("test skipped")) {
                        val elements = abortRatherThanSkipLines(line)
                        addAll(elements)
                    } else add(line)
                }
            }
    }

private val expected = listOf(
    "plan started",
    "test started: ENGINE_NAME",
    "test started: TEST_NAME",
    "test started: ROOT_NAME",
    "test started: test in root",
    "test successful: test in root",
    "test started: failing test in root",
    "test failed: failing test in root",
    "java.lang.RuntimeException: fail in root",
    "test started: inner",
    "test started: test in inner",
    "test successful: test in inner",
    "test started: failing test in inner",
    "test failed: failing test in inner",
    "java.lang.RuntimeException: fail in inner",
    "test successful: inner",
    "test skipped: skipped context",
    "test started: context with skipped test",
    "test skipped: SHOULD NOT BE SEEN",
    "test successful: context with skipped test",
    "test successful: ROOT_NAME",
    "test successful: TEST_NAME",
    "test successful: ENGINE_NAME",
    "plan finished"
)

// We abort rather than actually skip with some test runners, so the log is different
private fun abortRatherThanSkipLines(line: String): List<String> {
    val name = line.split(':')[1].trim()
    return listOf("test started: $name", "test aborted: $name")
}