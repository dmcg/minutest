package com.oneeyedmen.minutest.junit

import com.oneeyedmen.minutest.experimental.TopLevelContextBuilder
import io.github.classgraph.ClassGraph
import io.github.classgraph.ClassRefTypeSignature
import io.github.classgraph.FieldInfo
import io.github.classgraph.TypeSignature
import org.junit.platform.engine.DiscoveryFilter
import org.junit.platform.engine.DiscoverySelector
import org.junit.platform.engine.EngineDiscoveryRequest
import org.junit.platform.engine.Filter
import org.junit.platform.engine.discovery.ClassNameFilter
import org.junit.platform.engine.discovery.ClassSelector
import org.junit.platform.engine.discovery.DirectorySelector
import org.junit.platform.engine.discovery.PackageSelector
import kotlin.reflect.KProperty0
import kotlin.reflect.KVisibility.PUBLIC
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.kotlinProperty


inline fun <reified T : DiscoverySelector> EngineDiscoveryRequest.getSelectorsByType(): List<T> =
    getSelectorsByType(T::class.java)

inline fun <U, reified T : DiscoveryFilter<U>> EngineDiscoveryRequest.getFiltersByType(): Filter<U> =
    Filter.composeFilters(getFiltersByType(T::class.java))

inline fun <reified T : DiscoverySelector> EngineDiscoveryRequest.forEach(block: (T) -> Unit) {
    getSelectorsByType<T>().forEach(block)
}

fun scan(rq: EngineDiscoveryRequest): List<TestPackageDescriptor> {
    val scanner = ClassGraph()
        .enableClassInfo()
        .enableFieldInfo()
        .ignoreFieldVisibility()
        .disableJarScanning()
        .disableNestedJarScanning()
    
    rq.forEach<PackageSelector> { scanner.whitelistPackages(it.packageName) }
    rq.forEach<ClassSelector> { scanner.whitelistClasses(it.className) }
    rq.forEach<DirectorySelector> { scanner.whitelistPaths(it.rawPath) }
    
    val scanned = scanner.scan()
    
    return scanned
        .allClasses
        .filter { rq.getFiltersByType<String,ClassNameFilter>().apply(it.name).included() }
        .flatMap { it.declaredFieldInfo }
        .filter { it.isTopLevelContext() }
        .mapNotNull { it.toKotlinProperty() }
        .filter { it.visibility == PUBLIC }
        .map { property -> TopLevelContextDescriptor(property) }
        .groupBy { it.property.javaField?.declaringClass?.`package`?.name ?: "<tests>" }
        .map { (packageName, tests) -> TestPackageDescriptor(packageName, tests) }
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
