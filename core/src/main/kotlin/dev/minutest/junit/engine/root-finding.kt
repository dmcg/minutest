package dev.minutest.junit.engine

import dev.minutest.Node
import dev.minutest.internal.toRootContext
import org.junit.platform.engine.*
import org.junit.platform.engine.discovery.*
import kotlin.reflect.KClass

internal fun findRootNodes(
    root: MinutestEngineDescriptor,
    discoveryRequest: EngineDiscoveryRequest
): List<TestDescriptor> =
    when {
        discoveryRequest.getSelectorsByType<MethodSelector>().isNotEmpty() ->
            emptyList() // Cannot select by method
        else ->
            findRootNodes(discoveryRequest)
                .map { rootContext -> MinutestNodeDescriptor(root, rootContext.toRootContext()) }
                .filter { discoveryRequest.selectsByUniqueId(it) }
    }

private fun findRootNodes(
    discoveryRequest: EngineDiscoveryRequest
): List<Node<Unit>> =
    scanForRootNodes(
        scannerConfig = {
            discoveryRequest.forEach<PackageSelector> {
                whitelistPackages(it.packageName)
            }
            discoveryRequest.forEach<ClassSelector> {
                whitelistClasses(it.className)
            }
            discoveryRequest.forEach<DirectorySelector> {
                whitelistPaths(it.rawPath)
            }
        },
        classFilter = {
            discoveryRequest.getFiltersByType<ClassNameFilter>().apply(it.name).included() &&
                discoveryRequest.getFiltersByType<PackageNameFilter>().apply(it.packageName).included()
        }
    )

private inline fun <reified T : DiscoverySelector> EngineDiscoveryRequest.forEach(
    block: (T) -> Unit
) {
    getSelectorsByType<T>().forEach(block)
}

private inline fun <reified T : DiscoveryFilter<String>> EngineDiscoveryRequest.getFiltersByType()
    : Filter<String> =
    combineFiltersByType(T::class)

private fun EngineDiscoveryRequest.combineFiltersByType(
    filterClass: KClass<out DiscoveryFilter<String>>
): Filter<String> =
    Filter.composeFilters(getFiltersByType(filterClass.java))