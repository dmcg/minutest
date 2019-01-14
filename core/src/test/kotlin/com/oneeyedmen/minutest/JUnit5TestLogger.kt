package com.oneeyedmen.minutest

import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.TestExecutionResult.Status.FAILED
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.TestIdentifier
import org.junit.platform.launcher.TestPlan

internal class JUnit5TestLogger : TestExecutionListener {
    private val _log = mutableListOf<String>()
    
    val log: List<String> get() = _log.toList()
    
    override fun testPlanExecutionStarted(testPlan: TestPlan) {
        log("plan started")
    }
    
    override fun dynamicTestRegistered(testIdentifier: TestIdentifier) {
        log("test registered", testIdentifier)
    }
    
    override fun executionStarted(testIdentifier: TestIdentifier) {
        log("test started", testIdentifier)
    }
    
    override fun executionFinished(testIdentifier: TestIdentifier, testExecutionResult: TestExecutionResult) {
        log("test ${testExecutionResult.status.name.toLowerCase()}", testIdentifier)
        if (testExecutionResult.status == FAILED)
            log(testExecutionResult.throwable.get().toString())
    }
    
    override fun executionSkipped(testIdentifier: TestIdentifier, reason: String?) {
        log("test skipped", testIdentifier)
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