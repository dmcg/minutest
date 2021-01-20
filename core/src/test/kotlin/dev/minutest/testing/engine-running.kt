package dev.minutest.testing

import dev.minutest.JUnit5TestLogger
import org.junit.platform.engine.DiscoverySelector
import org.junit.platform.engine.discovery.DiscoverySelectors
import org.junit.platform.launcher.EngineFilter
import org.junit.platform.launcher.LauncherDiscoveryRequest
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder
import org.junit.platform.launcher.core.LauncherFactory

inline fun <reified T : Any> runTestsInClass(engineID: String): List<String> =
    runTestsInClass(engineID, T::class.java.name)

fun runTestsInClass(
    engineID: String,
    className: String
): List<String> =
    runTestsInClass(
        discoveryRequest(engineID, DiscoverySelectors.selectClass(className))
    )

private fun runTestsInClass(discoveryRequest: LauncherDiscoveryRequest): List<String> {
    val listener = JUnit5TestLogger()
    LauncherFactory.create().execute(discoveryRequest, listener)
    return listener.log
}

private fun discoveryRequest(
    engineID: String,
    selector: DiscoverySelector
) =
    LauncherDiscoveryRequestBuilder.request()
        .filters(EngineFilter.includeEngines(engineID))
        .selectors(selector)
        .build()