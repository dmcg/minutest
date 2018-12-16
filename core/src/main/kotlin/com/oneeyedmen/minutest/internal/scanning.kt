package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.NodeBuilder
import com.oneeyedmen.minutest.RuntimeContext
import io.github.classgraph.*
import kotlin.reflect.KFunction0
import kotlin.reflect.KVisibility.PUBLIC
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.kotlinFunction

internal fun scan(scannerConfig: ClassGraph.() -> Unit, classFilter: (ClassInfo) -> Boolean = {true}): List<RuntimeContext<Unit>> {
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
        .map { (packageName, functions) -> rootFor(packageName, functions) }
}

private fun MethodInfo.toKotlinFunction(): KFunction0<NodeBuilder<Unit, *>>? {
    @Suppress("UNCHECKED_CAST")
    return loadClassAndGetMethod().kotlinFunction as? KFunction0<NodeBuilder<Unit, *>>
}

private fun MethodInfo.definesTopLevelContext() =
    isStatic && isPublic && parameterInfo.isEmpty() && !isBridge
        && typeSignatureOrTypeDescriptor.resultType.name() == NodeBuilder::class.java.name

private fun TypeSignature.name() =
    (this as? ClassRefTypeSignature)?.baseClassName


internal fun rootFor(packageName: String, contextFuns: List<KFunction0<NodeBuilder<Unit, *>>>): RuntimeContext<Unit> {
    val rootContextBuilders: List<TopLevelContextBuilder<*>> = contextFuns
        .map { fn ->
            (fn() as TopLevelContextBuilder<*>).copy(name = fn.name)
            // TODO - we should be finding TopLevelContextBuilder not NodeBuilder
        }
    return PreparedRuntimeContext<Unit, Unit>(
        name = packageName,
        parent = null,
        fixtureFactory = { _, _ -> Unit },
        childBuilders = rootContextBuilders)
}