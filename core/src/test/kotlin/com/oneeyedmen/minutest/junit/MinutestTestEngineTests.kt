package com.oneeyedmen.minutest.junit

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.platform.engine.EngineExecutionListener
import org.junit.platform.engine.ExecutionRequest
import org.junit.platform.engine.TestDescriptor
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.UniqueId
import org.junit.platform.engine.discovery.DiscoverySelectors
import org.junit.platform.engine.reporting.ReportEntry
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request

class MinutestTestEngineTests {
    @Test
    fun `finds and executes "pure minutest" tests`() {
        val engine = MinutestTestEngine()
        val discoveryRequest = request()
            .selectors(DiscoverySelectors.selectPackage("example"))
            .build()
        
        val tests = engine.discover(discoveryRequest, UniqueId.forEngine(MinutestTestEngine.id))
        
        val listener = TestLogger()
        
        engine.execute(ExecutionRequest(tests, listener, discoveryRequest.configurationParameters))
        
        Assertions.assertEquals(
            listOf(
                "started: [engine:minutest]",
                "started: [engine:minutest]/[minutest-package:example]",
                "started: [engine:minutest]/[minutest-package:example]/[minutest-property:example context]",
                "registered: [engine:minutest]/[minutest-package:example]/[minutest-property:example context]/[minutest-test:a failing test]",
                "started: [engine:minutest]/[minutest-package:example]/[minutest-property:example context]/[minutest-test:a failing test]",
                "failed: [engine:minutest]/[minutest-package:example]/[minutest-property:example context]/[minutest-test:a failing test], threw: org.opentest4j.AssertionFailedError: example failure",
                "registered: [engine:minutest]/[minutest-package:example]/[minutest-property:example context]/[minutest-test:a passing test]",
                "started: [engine:minutest]/[minutest-package:example]/[minutest-property:example context]/[minutest-test:a passing test]",
                "successful: [engine:minutest]/[minutest-package:example]/[minutest-property:example context]/[minutest-test:a passing test]",
                "successful: [engine:minutest]/[minutest-package:example]/[minutest-property:example context]",
                "started: [engine:minutest]/[minutest-package:example]/[minutest-property:example typed context]",
                "registered: [engine:minutest]/[minutest-package:example]/[minutest-property:example typed context]/[minutest-test:a typed fixture test]",
                "started: [engine:minutest]/[minutest-package:example]/[minutest-property:example typed context]/[minutest-test:a typed fixture test]",
                "successful: [engine:minutest]/[minutest-package:example]/[minutest-property:example typed context]/[minutest-test:a typed fixture test]",
                "successful: [engine:minutest]/[minutest-package:example]/[minutest-property:example typed context]",
                "successful: [engine:minutest]/[minutest-package:example]",
                "successful: [engine:minutest]"),
            listener.log
        )
    }
}

class TestLogger : EngineExecutionListener {
    private val _log = mutableListOf<String>()
    
    val log: List<String> get() = _log.toList()
    
    override fun dynamicTestRegistered(testDescriptor: TestDescriptor) {
        log("registered", testDescriptor)
    }
    
    override fun executionStarted(testDescriptor: TestDescriptor) {
        log("started", testDescriptor)
    }
    
    override fun executionFinished(testDescriptor: TestDescriptor, testExecutionResult: TestExecutionResult) {
        log(testExecutionResult.status.name.toLowerCase(), testDescriptor, testExecutionResult.throwable.orElse(null))
    }
    
    override fun executionSkipped(testDescriptor: TestDescriptor, reason: String?) {
        log("skipped", testDescriptor)
    }
    
    private fun log(event: String, testDescriptor: TestDescriptor, failure: Throwable? = null) {
        _log.add("$event: ${testDescriptor.uniqueId}${failure?.let { ", threw: $it" }.orEmpty()}")
    }
    
    override fun reportingEntryPublished(testDescriptor: TestDescriptor, entry: ReportEntry) {
        TODO("should not be called")
    }
}
