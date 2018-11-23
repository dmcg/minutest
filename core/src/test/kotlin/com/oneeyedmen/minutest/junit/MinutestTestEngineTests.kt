package com.oneeyedmen.minutest.junit

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.discovery.ClassNameFilter.*
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage
import org.junit.platform.engine.discovery.DiscoverySelectors.selectUniqueId
import org.junit.platform.launcher.EngineFilter
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.TestIdentifier
import org.junit.platform.launcher.TestPlan
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request
import org.junit.platform.launcher.core.LauncherFactory

class MinutestTestEngineTests {
    @Test
    fun `selects tests by package`() {
        assertTestRun({ selectors(selectPackage("example.a")) },
            "test plan started",
            "test started: Minutest",
            "test started: example.a",
            "test started: example context",
            "test registered: a failing test",
            "test started: a failing test",
            "test failed: a failing test",
            "test registered: a passing test",
            "test started: a passing test",
            "test successful: a passing test",
            "test successful: example context",
            "test started: example typed context",
            "test registered: a typed fixture test",
            "test started: a typed fixture test",
            "test successful: a typed fixture test",
            "test successful: example typed context",
            "test successful: example.a",
            "test successful: Minutest",
            "test plan finished"
        )
    }
    
    @Test
    fun `select tests by class name`() {
        assertTestRun({ selectors(selectClass("example.a.ExampleMinutest")) },
            "test plan started",
            "test started: Minutest",
            "test started: example.a",
            "test started: example context",
            "test registered: a failing test",
            "test started: a failing test",
            "test failed: a failing test",
            "test registered: a passing test",
            "test started: a passing test",
            "test successful: a passing test",
            "test successful: example context",
            "test successful: example.a",
            "test successful: Minutest",
            "test plan finished"
        )
    }
    
    @Test
    fun `select tests by class name pattern`() {
        assertTestRun(
            {
                selectors(
                    selectPackage("example.a")
                ).filters(
                    excludeClassNamePatterns(".*Typed.*")
                )
            },
            "test plan started",
            "test started: Minutest",
            "test started: example.a",
            "test started: example context",
            "test registered: a failing test",
            "test started: a failing test",
            "test failed: a failing test",
            "test registered: a passing test",
            "test started: a passing test",
            "test successful: a passing test",
            "test successful: example context",
            "test successful: example.a",
            "test successful: Minutest",
            "test plan finished"
        )
    }
    
    @Test
    fun `select tests by unique id`() {
        assertTestRun({ selectors(selectUniqueId("[engine:minutest]/[minutest-package:example.a]/[minutest-context:example context]/[minutest-test:a passing test]")) },
            "test plan started",
            "test started: Minutest",
            "test started: example.a",
            "test started: example context",
            "test registered: a passing test",
            "test started: a passing test",
            "test successful: a passing test",
            "test successful: example context",
            "test successful: example.a",
            "test successful: Minutest",
            "test plan finished"
        )
    }
    
    private fun assertTestRun(config: LauncherDiscoveryRequestBuilder.() -> Unit, vararg expectedLog: String) {
        val listener = TestLogger()
        
        LauncherFactory.create().execute(
            request()
                .filters(EngineFilter.includeEngines(MinutestTestEngine.engineId))
                .apply(config)
                .build(),
            listener)
        
        assertEquals(expectedLog.asList().joinToString("\n"), listener.log.joinToString("\n"))
    }
}

private class TestLogger : TestExecutionListener {
    private val _log = mutableListOf<String>()
    
    val log: List<String> get() = _log.toList()
    
    override fun testPlanExecutionStarted(testPlan: TestPlan) {
        log("test plan started")
    }
    
    override fun dynamicTestRegistered(testIdentifier: TestIdentifier) {
        log("test registered", testIdentifier)
    }
    
    override fun executionStarted(testIdentifier: TestIdentifier) {
        log("test started", testIdentifier)
    }
    
    override fun executionFinished(testIdentifier: TestIdentifier, testExecutionResult: TestExecutionResult) {
        log("test ${testExecutionResult.status.name.toLowerCase()}", testIdentifier)
        
    }
    
    override fun executionSkipped(testIdentifier: TestIdentifier, reason: String?) {
        log("test skipped", testIdentifier)
    }
    
    override fun testPlanExecutionFinished(testPlan: TestPlan) {
        log("test plan finished")
    }
    
    private fun log(event: String, testIdentifier: TestIdentifier) {
        log("$event: ${testIdentifier.displayName}")
    }
    
    private fun log(message: String) {
        _log.add(message)
    }
}
