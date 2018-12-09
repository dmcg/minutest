package com.oneeyedmen.minutest.junit

import com.oneeyedmen.minutest.LoadedRuntimeContext
import com.oneeyedmen.minutest.Named
import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.RuntimeNode
import com.oneeyedmen.minutest.experimental.TopLevelContextBuilder
import io.github.classgraph.ClassGraph
import io.github.classgraph.ClassInfo
import io.github.classgraph.ClassRefTypeSignature
import io.github.classgraph.FieldInfo
import io.github.classgraph.TypeSignature
import org.junit.platform.engine.DiscoveryFilter
import org.junit.platform.engine.DiscoverySelector
import org.junit.platform.engine.EngineDiscoveryRequest
import org.junit.platform.engine.Filter
import org.junit.platform.engine.TestDescriptor
import org.junit.platform.engine.UniqueId
import org.junit.platform.engine.discovery.ClassNameFilter
import org.junit.platform.engine.discovery.ClassSelector
import org.junit.platform.engine.discovery.DirectorySelector
import org.junit.platform.engine.discovery.MethodSelector
import org.junit.platform.engine.discovery.PackageNameFilter
import org.junit.platform.engine.discovery.PackageSelector
import org.junit.platform.engine.discovery.UniqueIdSelector
import kotlin.reflect.KClass
import kotlin.reflect.KProperty0
import kotlin.reflect.KVisibility.PUBLIC
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.kotlinProperty


internal inline fun <reified T : DiscoverySelector> EngineDiscoveryRequest.forEach(block: (T) -> Unit) {
    getSelectorsByType<T>().forEach(block)
}

internal inline fun <reified T : DiscoverySelector> EngineDiscoveryRequest.getSelectorsByType(): List<T> =
    getSelectorsByType(T::class.java)

internal inline fun <reified T : DiscoveryFilter<String>> EngineDiscoveryRequest.getFiltersByType(): Filter<String> =
    combineFiltersByType(T::class)

internal fun EngineDiscoveryRequest.combineFiltersByType(filterClass: KClass<out DiscoveryFilter<String>>): Filter<String> =
    Filter.composeFilters(getFiltersByType(filterClass.java))

internal data class ScannedPackageContext(
    val packageName: String,
    private val contextProperties: List<KProperty0<TopLevelContextBuilder>>,
    override val properties: Map<Any, Any> = emptyMap()

) : RuntimeContext() {
    
    override val parent: Named? = null
    override val name: String get() = packageName
    override val children: List<RuntimeNode> by lazy { contextProperties.map { p -> p.get().build(p.name) } }
    
    override fun withChildren(children: List<RuntimeNode>): RuntimeContext {
        return LoadedRuntimeContext(name, parent, emptyMap(), children, {})
    }
    
    override fun withProperties(properties: Map<Any, Any>): RuntimeNode {
        return copy(properties = properties)
    }
    
    override fun close() {
    }
}

internal fun scan(root: MinutestEngineDescriptor, rq: EngineDiscoveryRequest): List<TestDescriptor> {
    // Cannot select by method
    if (rq.getSelectorsByType<MethodSelector>().isNotEmpty()) {
        return emptyList()
    }
    
    return scan(
        scannerConfig = {
            rq.forEach<PackageSelector> { whitelistPackages(it.packageName) }
            rq.forEach<ClassSelector> { whitelistClasses(it.className) }
            rq.forEach<DirectorySelector> { whitelistPaths(it.rawPath) }
        },
        classFilter = {
            rq.getFiltersByType<ClassNameFilter>().apply(it.name).included() &&
                rq.getFiltersByType<PackageNameFilter>().apply(it.packageName).included()
        })
        .map { MinutestNodeDescriptor(root, it) }
        .filter { rq.selectsByUniqueId(it) }
}

private fun scan(scannerConfig: ClassGraph.() -> Unit, classFilter: (ClassInfo) -> Boolean): List<ScannedPackageContext> {
    return ClassGraph()
        .enableClassInfo()
        .enableFieldInfo()
        .ignoreFieldVisibility()
        .disableJarScanning()
        .disableNestedJarScanning()
        .apply(scannerConfig)
        .scan()
        .allClasses
        .filter(classFilter)
        .flatMap { it.declaredFieldInfo }
        .filter { it.isTopLevelContext() }
        .mapNotNull { it.toKotlinProperty() }
        .filter { it.visibility == PUBLIC }
        .groupBy { it.javaField?.declaringClass?.`package`?.name ?: "<tests>" }
        .map { (packageName, properties) -> ScannedPackageContext(packageName, properties) }
}

private fun FieldInfo.toKotlinProperty(): KProperty0<TopLevelContextBuilder>? {
    @Suppress("UNCHECKED_CAST")
    return loadClassAndGetField().kotlinProperty as? KProperty0<TopLevelContextBuilder>
}

private fun FieldInfo.isTopLevelContext() =
    isStatic && typeSignatureOrTypeDescriptor.fieldTypeName() == TopLevelContextBuilder::class.java.name

private fun TypeSignature.fieldTypeName() =
    when (this) {
        is ClassRefTypeSignature -> baseClassName
        else -> null
    }

internal fun EngineDiscoveryRequest.selectsByUniqueId(descriptor: TestDescriptor) =
    getSelectorsByType<UniqueIdSelector>()
        .run { isEmpty() || any { selector -> descriptor.uniqueId.overlaps(selector.uniqueId) } }

internal fun UniqueId.overlaps(that: UniqueId) =
    this.hasPrefix(that) || that.hasPrefix(this)
