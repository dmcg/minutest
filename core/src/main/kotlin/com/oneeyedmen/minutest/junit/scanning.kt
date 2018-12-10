package com.oneeyedmen.minutest.junit

import com.oneeyedmen.minutest.*
import io.github.classgraph.*
import org.junit.platform.engine.*
import org.junit.platform.engine.TestDescriptor
import org.junit.platform.engine.discovery.*
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
    private val contextProperties: List<KProperty0<NodeBuilder<Unit>>>,
    override val properties: Map<Any, Any> = emptyMap()

) : RuntimeContext() {
    
    override val parent: Named? = null
    override val name: String get() = packageName
    override val children: List<RuntimeNode> by lazy {
        contextProperties.map { p ->
            val rootWithDefaultName = (p.get().buildRootNode() as? RuntimeContext)
                ?: error("Can't yet have tests at top level")
            LoadedRuntimeContext(rootWithDefaultName, name = p.name)
        }
    }
    
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

private fun FieldInfo.toKotlinProperty(): KProperty0<NodeBuilder<Unit>>? {
    @Suppress("UNCHECKED_CAST")
    return loadClassAndGetField().kotlinProperty as? KProperty0<NodeBuilder<Unit>>
}

private fun FieldInfo.isTopLevelContext() =
    isStatic && typeSignatureOrTypeDescriptor.fieldTypeName() == NodeBuilder::class.java.name

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
