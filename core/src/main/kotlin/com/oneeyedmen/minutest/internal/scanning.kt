package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.LoadedRuntimeContext
import com.oneeyedmen.minutest.LoadedRuntimeTest
import com.oneeyedmen.minutest.Named
import com.oneeyedmen.minutest.NodeBuilder
import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.RuntimeNode
import com.oneeyedmen.minutest.RuntimeTest
import com.oneeyedmen.minutest.buildRootNode
import io.github.classgraph.ClassGraph
import io.github.classgraph.ClassInfo
import io.github.classgraph.ClassRefTypeSignature
import io.github.classgraph.MethodInfo
import io.github.classgraph.TypeSignature
import kotlin.reflect.KFunction0
import kotlin.reflect.KVisibility.PUBLIC
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.kotlinFunction

internal data class ScannedPackageContext(
    val packageName: String,
    private val contextFuns: List<KFunction0<NodeBuilder<Unit>>>,
    override val properties: Map<Any, Any> = emptyMap()

) : RuntimeContext() {
    
    override val parent: Named? = null
    override val name: String get() = packageName
    override val children: List<RuntimeNode> by lazy {
        contextFuns.map { f ->
            val rootWithDefaultName = f().buildRootNode()
            when (rootWithDefaultName) {
                is RuntimeContext -> LoadedRuntimeContext(delegate = rootWithDefaultName, name = f.name)
                is RuntimeTest -> LoadedRuntimeTest(delegate = rootWithDefaultName, name = f.name)
            }
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

internal fun scan(scannerConfig: ClassGraph.() -> Unit, classFilter: (ClassInfo) -> Boolean = {true}): List<ScannedPackageContext> {
    return ClassGraph()
        .enableClassInfo()
        .enableMethodInfo()
        .disableJarScanning()
        .disableNestedJarScanning()
        .apply(scannerConfig)
        .scan()
        .allClasses
        .filter { it.isStandardClass && !it.isAnonymousInnerClass && it.isPublic && !it.isSynthetic }
        .filter(classFilter)
        .flatMap { it.declaredMethodInfo }
        .filter { it.definesTopLevelContext() }
        .mapNotNull { it.toKotlinFunction() }
        .filter { it.visibility == PUBLIC }
        .groupBy { it.javaMethod?.declaringClass?.`package`?.name ?: "<tests>" }
        .map { (packageName, functions) -> ScannedPackageContext(packageName, functions) }
}

private fun MethodInfo.toKotlinFunction(): KFunction0<NodeBuilder<Unit>>? {
    @Suppress("UNCHECKED_CAST")
    return loadClassAndGetMethod().kotlinFunction as? KFunction0<NodeBuilder<Unit>>
}

private fun MethodInfo.definesTopLevelContext() =
    isStatic && isPublic && parameterInfo.isEmpty() && !isBridge
        && typeSignatureOrTypeDescriptor.resultType.name() == NodeBuilder::class.java.name

private fun TypeSignature.name() =
    (this as? ClassRefTypeSignature)?.baseClassName

