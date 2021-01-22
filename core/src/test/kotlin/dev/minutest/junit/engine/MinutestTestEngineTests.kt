package dev.minutest.junit.engine

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.discovery.ClassNameFilter.excludeClassNamePatterns
import org.junit.platform.engine.discovery.ClassNameFilter.includeClassNamePatterns
import org.junit.platform.engine.discovery.DiscoverySelectors.*
import org.junit.platform.engine.discovery.PackageNameFilter.excludePackageNames
import org.junit.platform.launcher.*
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request
import org.junit.platform.launcher.core.LauncherFactory


class MinutestTestEngineTests {

    @Test
    fun `selects tests by package`() {
        assertTestRun(
            { selectors(selectPackage("samples.minutestRunner.a")) },
            "plan started",
            "started: Minutest",
            "started: example context in class",
            "registered: a passing test",
            "started: a passing test",
            "successful: a passing test",
            "successful: example context in class",
            "started: samples.minutestRunner.a",
            "registered: example context",
            "started: example context",
            "registered: a failing test",
            "started: a failing test",
            "failed: a failing test",
            "org.opentest4j.AssertionFailedError: example failure",
            "registered: a passing test",
            "started: a passing test",
            "successful: a passing test",
            "successful: example context",
            "registered: example skipped context",
            "started: example skipped context",
            "aborted: example skipped context",
            "registered: example typed context",
            "started: example typed context",
            "registered: a typed fixture test",
            "started: a typed fixture test",
            "successful: a typed fixture test",
            "successful: example typed context",
            "successful: samples.minutestRunner.a",
            "successful: Minutest",
            "plan finished"
        )
    }

    @Test
    fun `initial test plan contains the packages discovered to declare top level contexts`() {
        assertDiscovered(
            { selectors(selectPackage("samples.minutestRunner.a")) },
            "Minutest",
            "example context in class",
            "samples.minutestRunner.a"
        )
    }

    @Test
    fun `select tests by class name`() {
        assertTestRun(
            { selectors(selectClass("samples.minutestRunner.a.ExampleMinutest")) },
            "plan started",
            "started: Minutest",
            "started: samples.minutestRunner.a",
            "registered: example context",
            "started: example context",
            "registered: a failing test",
            "started: a failing test",
            "failed: a failing test",
            "org.opentest4j.AssertionFailedError: example failure",
            "registered: a passing test",
            "started: a passing test",
            "successful: a passing test",
            "successful: example context",
            "successful: samples.minutestRunner.a",
            "successful: Minutest",
            "plan finished"
        )
    }

    @Test
    fun `select tests by class name for tests in class`() {
        assertTestRun(
            { selectors(selectClass("samples.minutestRunner.a.ExampleMinutestInClass")) },
            "plan started",
            "started: Minutest",
            "started: samples.minutestRunner.a.ExampleMinutestInClass",
            "registered: example context in class",
            "started: example context in class",
            "registered: a passing test",
            "started: a passing test",
            "successful: a passing test",
            "successful: example context in class",
            "successful: samples.minutestRunner.a.ExampleMinutestInClass",
            "successful: Minutest",
            "plan finished",
        )
    }

    @Test
    fun `select tests by class name pattern`() {
        assertTestRun(
            {
                selectors(selectPackage("samples.minutestRunner.a"))
                filters(
                    excludeClassNamePatterns(".*Typed.*"),
                    excludeClassNamePatterns(".*Skipped.*")
                )
            },
            "plan started",
            "started: Minutest",
            "started: example context in class",
            "registered: a passing test",
            "started: a passing test",
            "successful: a passing test",
            "successful: example context in class",
            "started: samples.minutestRunner.a",
            "registered: example context",
            "started: example context",
            "registered: a failing test",
            "started: a failing test",
            "failed: a failing test",
            "org.opentest4j.AssertionFailedError: example failure",
            "registered: a passing test",
            "started: a passing test",
            "successful: a passing test",
            "successful: example context",
            "successful: samples.minutestRunner.a",
            "successful: Minutest",
            "plan finished"
        )
    }

    @Test
    fun `skip on root`() {
        assertTestRun(
            {
                selectors(selectPackage("samples.minutestRunner.a"))
                filters(includeClassNamePatterns(".*ExampleSkippedMinutest.*"))
            },
            "plan started",
            "started: Minutest",
            "started: samples.minutestRunner.a",
            "registered: example skipped context",
            "started: example skipped context",
            "aborted: example skipped context",
            "successful: samples.minutestRunner.a",
            "successful: Minutest",
            "plan finished"
        )
    }

    @Test
    fun `filter tests by package name`() {
        assertTestRun(
            {
                selectors(selectPackage("samples.minutestRunner"))
                filters(excludePackageNames("samples.minutestRunner.c"))
            },
            "plan started",
            "started: Minutest",
            "started: example context in class",
            "registered: a passing test",
            "started: a passing test",
            "successful: a passing test",
            "successful: example context in class",
            "started: samples.minutestRunner.a",
            "registered: example context",
            "started: example context",
            "registered: a failing test",
            "started: a failing test",
            "failed: a failing test",
            "org.opentest4j.AssertionFailedError: example failure",
            "registered: a passing test",
            "started: a passing test",
            "successful: a passing test",
            "successful: example context",
            "registered: example skipped context",
            "started: example skipped context",
            "aborted: example skipped context",
            "registered: example typed context",
            "started: example typed context",
            "registered: a typed fixture test",
            "started: a typed fixture test",
            "successful: a typed fixture test",
            "successful: example typed context",
            "successful: samples.minutestRunner.a",
            "successful: Minutest",
            "plan finished"
        )
    }

    @Test
    fun `select tests by unique id`() {
        val uniqueIdSelector =
            selectUniqueId("[engine:minutest]/[minutest-context:samples.minutestRunner.a]/[minutest-context:example context]/[minutest-test:a passing test]")

        assertDiscovered(
            { selectors(uniqueIdSelector) },
            "Minutest",
            "samples.minutestRunner.a"
        )

        assertTestRun(
            { selectors(uniqueIdSelector) },
            "plan started",
            "started: Minutest",
            "started: samples.minutestRunner.a",
            "registered: example context",
            "started: example context",
            "registered: a passing test",
            "started: a passing test",
            "successful: a passing test",
            "successful: example context",
            "successful: samples.minutestRunner.a",
            "successful: Minutest",
            "plan finished"
        )
    }

    @Test
    fun `returns no tests if discovery request selects by method`() {
        val methodSelector = selectMethod("samples.minutestRunner.c.ExampleForMethodSelection#root1()")

        assertDiscovered(
            { selectors(methodSelector) },
            "Minutest",
            "samples.minutestRunner.c.ExampleForMethodSelection"
        )

        assertTestRun(
            { selectors(methodSelector) },
            "plan started",
            "started: Minutest",
            "started: samples.minutestRunner.c.ExampleForMethodSelection",
            "registered: root1",
            "started: root1",
            "registered: a passing test",
            "started: a passing test",
            "successful: a passing test",
            "successful: root1",
            "successful: samples.minutestRunner.c.ExampleForMethodSelection",
            "successful: Minutest",
            "plan finished",
        )
    }

    private fun assertDiscovered(config: LauncherDiscoveryRequestBuilder.() -> Unit, vararg expectedTests: String) {
        val tests = performDiscovery(config)
            .roots
            .flatMap { setOf(it) + performDiscovery(config).getDescendants(it) }
            .map { it.displayName }
            .toSet()

        assertEquals(expectedTests.toSet(), tests, "discovered")
    }

    private fun assertTestRun(config: LauncherDiscoveryRequestBuilder.() -> Unit, vararg expectedLog: String) {
        val listener = TestLogger()
        LauncherFactory.create().execute(discoveryRequest(config), listener)
        assertEquals(expectedLog.asList().joinToString("\n"), listener.log.joinToString("\n"), "executed")
    }

    private fun performDiscovery(config: LauncherDiscoveryRequestBuilder.() -> Unit) =
        LauncherFactory.create().discover(discoveryRequest(config))

    private fun discoveryRequest(config: LauncherDiscoveryRequestBuilder.() -> Unit): LauncherDiscoveryRequest {
        return request()
            .filters(EngineFilter.includeEngines(MinutestTestEngine.engineId))
            .apply(config)
            .build()
    }
}

private class TestLogger : TestExecutionListener {
    private val _log = mutableListOf<String>()

    val log: List<String> get() = _log.toList()

    override fun testPlanExecutionStarted(testPlan: TestPlan) {
        log("plan started")
    }

    override fun dynamicTestRegistered(testIdentifier: TestIdentifier) {
        log("registered", testIdentifier)
    }

    override fun executionStarted(testIdentifier: TestIdentifier) {
        log("started", testIdentifier)
    }

    override fun executionFinished(testIdentifier: TestIdentifier, testExecutionResult: TestExecutionResult) {
        log(testExecutionResult.status.name.toLowerCase(), testIdentifier)
        if (testExecutionResult.status == TestExecutionResult.Status.FAILED)
            log(testExecutionResult.throwable.get().toString())
    }

    override fun executionSkipped(testIdentifier: TestIdentifier, reason: String?) {
        log("skipped", testIdentifier)
    }

    override fun testPlanExecutionFinished(testPlan: TestPlan) {
        log("plan finished")
    }

    private fun log(event: String, testIdentifier: TestIdentifier) {
        log("$event: ${testIdentifier.displayName}")
    }

    private fun log(message: String) {
        _log.add(message)
    }
}
