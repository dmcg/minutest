package com.oneeyedmen.minutest

import com.oneeyedmen.minutest.internal.TopLevelContextBuilder
import com.oneeyedmen.minutest.junit.toTestFactory
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest
import org.junit.platform.engine.discovery.DiscoverySelectors
import org.junit.platform.launcher.EngineFilter
import org.junit.platform.launcher.LauncherDiscoveryRequest
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder
import org.junit.platform.launcher.core.LauncherFactory
import java.util.stream.Stream
import kotlin.reflect.KClass

fun executeTests(tests: Stream<out DynamicNode>, exceptions: MutableList<Throwable> = mutableListOf()): List<Throwable> {
    tests.use {
        it.forEachOrdered { dynamicNode ->
            when (dynamicNode) {
                is DynamicTest -> try {
                    dynamicNode.executable.execute()
                } catch (x: Throwable) {
                    exceptions.add(x)
                }
                is DynamicContainer -> executeTests(dynamicNode.children, exceptions)
            }
        }
    }
    return exceptions
}

fun executeTests(root: TopLevelContextBuilder<*>) = executeTests(root.toTestFactory())

inline fun <reified T : Any> runTestsInClass(engineID: String): List<String> = runTestsInClass(T::class, engineID)

fun runTestsInClass(testClass: KClass<*>, engineID: String): List<String> {
    val listener = JUnit5TestLogger()
    LauncherFactory.create().execute(discoveryRequest(testClass, engineID), listener)
    return listener.log
}

private fun discoveryRequest(testClass: KClass<*>, engineID: String): LauncherDiscoveryRequest {
    return LauncherDiscoveryRequestBuilder.request()
        .filters(EngineFilter.includeEngines(engineID))
        .selectors(DiscoverySelectors.selectClass(testClass.java))
        .build()
}

