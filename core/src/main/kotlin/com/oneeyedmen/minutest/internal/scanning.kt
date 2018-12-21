package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.LoadedRuntimeContext
import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.RuntimeNode
import com.oneeyedmen.minutest.Test
import io.github.classgraph.*
import kotlin.reflect.KFunction0
import kotlin.reflect.KVisibility.PUBLIC
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.kotlinFunction

internal data class ScannedPackageContext(
    val packageName: String,
    private val contextFuns: List<KFunction0<TopLevelContextBuilder<*>>>,
    override val properties: Map<Any, Any> = emptyMap()

) : RuntimeContext() {
    override fun runTest(test: Test<*>, parentContext: ParentContext<*>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override val name: String get() = packageName
    override val children: List<RuntimeNode> by lazy {
        contextFuns.map { f ->
            val rootWithDefaultName = f().buildNode()
            LoadedRuntimeContext(delegate = rootWithDefaultName, name = f.name)
        }
    }
    
    override fun withChildren(children: List<RuntimeNode>): RuntimeContext {
        return LoadedRuntimeContext(name, emptyMap(), children, {_, _ -> Unit}, {})
    }
    
    override fun close() {}
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
        // Check Kotlin visibility because a public static Java method might have internal visibility in Kotlin
        .filter { it.visibility == PUBLIC }
        .groupBy { it.javaMethod?.declaringClass?.`package`?.name ?: "<tests>" }
        .map { (packageName, functions) -> ScannedPackageContext(packageName, functions) }
}

private fun MethodInfo.toKotlinFunction(): KFunction0<TopLevelContextBuilder<*>>? {
    @Suppress("UNCHECKED_CAST")
    return loadClassAndGetMethod().kotlinFunction as? KFunction0<TopLevelContextBuilder<*>>
}

private fun MethodInfo.definesTopLevelContext() =
    isStatic && isPublic && parameterInfo.isEmpty() && !isBridge
        && typeSignatureOrTypeDescriptor.resultType.name() == TopLevelContextBuilder::class.java.name

private fun TypeSignature.name() =
    (this as? ClassRefTypeSignature)?.baseClassName

