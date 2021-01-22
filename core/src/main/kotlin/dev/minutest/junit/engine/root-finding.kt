package dev.minutest.junit.engine

import dev.minutest.Node
import dev.minutest.internal.*
import dev.minutest.junit.JUnit5Minutests
import org.junit.platform.commons.annotation.Testable
import org.junit.platform.engine.*
import org.junit.platform.engine.discovery.*
import java.lang.reflect.Method
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

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
    shortcutClassSelection(discoveryRequest)
        ?: scanForRootNodes(
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

private inline fun <reified T : DiscoverySelector> EngineDiscoveryRequest.forEach(block: (T) -> Unit) {
    getSelectorsByType<T>().forEach(block)
}

private inline fun <reified T : DiscoveryFilter<String>> EngineDiscoveryRequest.getFiltersByType()
    : Filter<String> =
    combineFiltersByType(T::class)

private fun EngineDiscoveryRequest.combineFiltersByType(
    filterClass: KClass<out DiscoveryFilter<String>>
): Filter<String> =
    Filter.composeFilters(getFiltersByType(filterClass.java))

private fun shortcutClassSelection(discoveryRequest: EngineDiscoveryRequest): List<Node<Unit>>? {
    time("Minutest loading single test ") {
        val classSelectors = discoveryRequest.getSelectorsByType<ClassSelector>()
        return when {
            classSelectors.isEmpty() -> null
            else ->
                classSelectors.map {
                    amalgamatedRootContext(Class.forName(it.className))
                }.filterNotNull()
        }
    }
}

private fun amalgamatedRootContext(klass: Class<*>): Node<Unit>? {
    if (quickCheckForNotOurs(klass))
        return null
    val staticBuilders = klass.staticMethodsAsContextBuilderBuilders { it.hasTestableAnnotation }
    return when {
        staticBuilders.isNotEmpty() ->
            AmalgamatedRootContext(
                klass.`package`.name ?: error("Trying find tests in class with no name"),
                staticBuilders.map { method ->
                    method.invoke().buildNode()
                }
            )
        else -> klass.kotlin.constructors.singleOrNull()?.call()?.let { instance ->
            instance.rootContextFromMethods(flattenSingleNode = false) {
                it.hasTestableAnnotation
            }
        }
    }
}

private fun quickCheckForNotOurs(klass: Class<*>) =
    JUnit5Minutests::class.java.isAssignableFrom(klass)
        || klass.methods.none { it.hasTestableAnnotation }

private val Method.hasTestableAnnotation: Boolean
    get() = annotations.any { it is Testable }

private val KFunction<*>.hasTestableAnnotation
    get() =
        annotations.any { it is Testable }