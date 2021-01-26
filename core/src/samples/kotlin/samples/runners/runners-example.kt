package samples.runners

import dev.minutest.ContextBuilder
import dev.minutest.experimental.SKIP
import dev.minutest.experimental.minus
import dev.minutest.test
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
    substitutions: List<Pair<String, String>>,
    hasExtraRoot: Boolean = true,
    abortRatherThanSkip: Boolean = true,
    expectedLog: List<String>
): List<String> =
    expectedLog.map { line ->
        substitutions.fold(line) { it, sub -> it.replace(sub.first, sub.second) }
    }
        .filterNot { hasExtraRoot && it.endsWith("ROOT_NAME") }
        .fixSkipped(abortRatherThanSkip)

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

val expected = listOf(
    "plan started",
    "test started: ENGINE_NAME",
    "test started: TEST_NAME",
    "test started: ROOT_NAME", // only in JUnit5
    "test started: CONTEXT_NAME",
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
    "test successful: CONTEXT_NAME",
    "test successful: ROOT_NAME", // only in JUnit5
    "test successful: TEST_NAME",
    "test successful: ENGINE_NAME",
    "plan finished"
)

val multiRootExpected = listOf(
    "plan started",
    "test started: ENGINE_NAME",
    "test started: TEST_NAME",
    "test started: ROOT_NAME",
    "test started: moreTests",
    "test started: test in moreTests",
    "test successful: test in moreTests",
    "test successful: moreTests",
    "test started: tests",
    "test started: test in tests",
    "test successful: test in tests",
    "test successful: tests",
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