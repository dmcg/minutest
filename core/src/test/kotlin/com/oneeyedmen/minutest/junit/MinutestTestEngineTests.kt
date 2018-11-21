package com.oneeyedmen.minutest.junit

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.platform.engine.TestEngine
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.UniqueId
import org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage
import org.junit.platform.engine.support.descriptor.EngineDescriptor
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.TestIdentifier
import org.junit.platform.launcher.TestPlan
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request
import org.junit.platform.launcher.core.LauncherFactory

class MinutestTestEngineTests {
    @Test
    fun `finds and executes "pure minutest" tests`() {
        val launcher = LauncherFactory.create()
        
        val discoveryRequest = request()
            .selectors(selectPackage("example"))
            .build()
        
        val listener = TestLogger()
        
        launcher.execute(discoveryRequest, listener)
        
        Assertions.assertEquals(
            listOf(
                "test plan started",
                "test started: Minutest",
                "test started: example",
                "test started: example context",
                "test registered: a failing test",
                "test started: a failing test",
                "test finished: a failing test",
                "test registered: a passing test",
                "test started: a passing test",
                "test finished: a passing test",
                "test finished: example context",
                "test started: example typed context",
                "test registered: a typed fixture test",
                "test started: a typed fixture test",
                "test finished: a typed fixture test",
                "test finished: example typed context",
                "test finished: example",
                "test finished: Minutest",
                "test plan finished"
            ),
            listener.log
        )
    }
}

class TestLogger : TestExecutionListener {
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
        log("test finished", testIdentifier)
        
    }
    
    override fun executionSkipped(testIdentifier: TestIdentifier, reason: String?) {
        log("test skipped", testIdentifier)
    }
    
    override fun testPlanExecutionFinished(testPlan: TestPlan) {
        log("test plan finished")
    }
    
    private fun log(event: String, testIdentifier: TestIdentifier) {
        if (testIdentifier.uniqueId.startsWith("[engine:minutest]")) {
            log("$event: ${testIdentifier.displayName}")
        }
    }
    
    private fun log(message: String) {
        _log.add(message)
    }
}
