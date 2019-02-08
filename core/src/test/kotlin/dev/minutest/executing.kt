package dev.minutest

import dev.minutest.internal.TopLevelContextBuilder
import dev.minutest.junit.toTestFactory
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest
import org.junit.platform.engine.DiscoverySelector
import org.junit.platform.engine.discovery.DiscoverySelectors
import org.junit.platform.launcher.EngineFilter
import org.junit.platform.launcher.LauncherDiscoveryRequest
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder
import org.junit.platform.launcher.core.LauncherFactory
import java.util.stream.Stream

fun executeTests(tests: Stream<out DynamicNode>,
    exceptions: MutableList<Throwable> = mutableListOf()
): List<Throwable> {
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

inline fun <reified T : Any> runTestsInClass(engineID: String): List<String> = runTestsInClass(
    discoveryRequest(engineID, DiscoverySelectors.selectClass(T::class.java)))

fun runTestsInClass(className: String, engineID: String): List<String> = runTestsInClass(
    discoveryRequest(engineID, DiscoverySelectors.selectClass(className)))

fun runTestsInClass(discoveryRequest: LauncherDiscoveryRequest): List<String> {
    val listener = JUnit5TestLogger()
    LauncherFactory.create().execute(discoveryRequest, listener)
    return listener.log
}

fun discoveryRequest(engineID: String, selector: DiscoverySelector): LauncherDiscoveryRequest =
    LauncherDiscoveryRequestBuilder.request()
        .filters(EngineFilter.includeEngines(engineID))
        .selectors(selector)
        .build()

