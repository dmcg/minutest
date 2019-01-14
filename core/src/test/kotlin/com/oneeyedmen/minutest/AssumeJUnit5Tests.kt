package com.oneeyedmen.minutest

import example.junit5.AssumeInNestedContext
import example.junit5.AssumeInRootContext
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.engine.JupiterTestEngine
import org.junit.platform.engine.discovery.DiscoverySelectors
import org.junit.platform.launcher.EngineFilter
import org.junit.platform.launcher.LauncherDiscoveryRequest
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder
import org.junit.platform.launcher.core.LauncherFactory
import kotlin.reflect.KClass


class AssumeJUnit5Tests {
    private val listener = JUnit5TestLogger()
    
    @Test
    fun testRootContext() {
        run<AssumeInRootContext>()
        assertTrue(listener.log.any { it.startsWith("test aborted") }, this::reportLogOnFailure)
    }
    
    @Test
    fun testNestedContext() {
        run<AssumeInNestedContext>()
        assertTrue(listener.log.any { it.startsWith("test aborted") }, this::reportLogOnFailure)
    }
    
    private fun reportLogOnFailure() = listener.log.joinToString("\n")
    
    private inline fun <reified T : Any> run() {
        run(T::class)
    }
    
    private fun run(testClass: KClass<*>) {
        LauncherFactory.create().execute(discoveryRequest(testClass), listener)
    }
    
    private fun discoveryRequest(testClass: KClass<*>): LauncherDiscoveryRequest {
        return LauncherDiscoveryRequestBuilder.request()
            .filters(EngineFilter.includeEngines(JupiterTestEngine.ENGINE_ID))
            .selectors(DiscoverySelectors.selectClass(testClass.java))
            .build()
    }
}



