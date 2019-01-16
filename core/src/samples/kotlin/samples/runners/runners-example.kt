package samples.runners

import com.oneeyedmen.minutest.TestContext

fun TestContext<Unit>.runnersExample() {

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

}

fun expectedRunnersLog(engineName: String,
    testName: String,
    rootName: String,
    noRegistration: Boolean
): List<String> = expected
    .map {
        it.replace("ENGINE_NAME", engineName).replace("TEST_NAME", testName).replace("ROOT_NAME", rootName)
    }
    .filterNot {
        noRegistration && it.startsWith("test registered")
    }

private val expected = listOf(
    "plan started",
    "test started: ENGINE_NAME",
    "test started: TEST_NAME",
    "test started: ROOT_NAME",
    "test registered: test in root",
    "test started: test in root",
    "test successful: test in root",
    "test registered: failing test in root",
    "test started: failing test in root",
    "test failed: failing test in root",
    "java.lang.RuntimeException: fail in root",
    "test registered: inner",
    "test started: inner",
    "test registered: test in inner",
    "test started: test in inner",
    "test successful: test in inner",
    "test registered: failing test in inner",
    "test started: failing test in inner",
    "test failed: failing test in inner",
    "java.lang.RuntimeException: fail in inner",
    "test successful: inner",
    "test successful: ROOT_NAME",
    "test successful: TEST_NAME",
    "test successful: ENGINE_NAME",
    "plan finished"
)