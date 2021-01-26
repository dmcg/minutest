package dev.minutest.junit.engine

import dev.minutest.internal.AmalgamatedRootContext
import dev.minutest.internal.rootContextForClass
import dev.minutest.internal.rootContextFromTopLevelFunctions
import dev.minutest.internal.toRootContext
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
    findRootNodes(discoveryRequest)
        .map { rootContext -> MinutestNodeDescriptor(root, rootContext.toRootContext()) }
        .filter { discoveryRequest.selectsByUniqueId(it) }

private fun findRootNodes(
    discoveryRequest: EngineDiscoveryRequest
): List<AmalgamatedRootContext> =
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

private fun shortcutClassSelection(discoveryRequest: EngineDiscoveryRequest): List<AmalgamatedRootContext>? {
    val classSelectors = selectorsFrom(discoveryRequest)
    return when {
        classSelectors.isEmpty() -> null
        else ->
            classSelectors.mapNotNull { classAndMethodName ->
                amalgamatedRootContext(Class.forName(classAndMethodName.className))
                    ?.selectJust(classAndMethodName.methodName)
            }
    }
}

private fun AmalgamatedRootContext.selectJust(methodName: String?): AmalgamatedRootContext =
    when (methodName) {
        null -> this
        else -> this.withFilteredChildren { it.name == methodName }
    }

private fun selectorsFrom(discoveryRequest: EngineDiscoveryRequest) =
    discoveryRequest.getSelectorsByType<ClassSelector>().map {
        ClassAndMethodNames(it.className, null)
    } +
        discoveryRequest.getSelectorsByType<MethodSelector>().map {
            ClassAndMethodNames(it.className, it.methodName)
        }


private fun amalgamatedRootContext(javaClass: Class<*>): AmalgamatedRootContext? {
    if (quickCheckForNotOurs(javaClass))
        return null
    return rootContextFromTopLevelFunctions(javaClass) {
        it.hasTestableAnnotation
    } ?: rootContextForClass(javaClass.kotlin) {
        it.hasTestableAnnotation
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

private class ClassAndMethodNames(val className: String, val methodName: String?)