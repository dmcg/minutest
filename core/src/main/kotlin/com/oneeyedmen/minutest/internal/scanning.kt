package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.RuntimeNode
import com.oneeyedmen.minutest.Test
import com.oneeyedmen.minutest.TestDescriptor
import io.github.classgraph.*
import kotlin.reflect.KFunction0
import kotlin.reflect.KVisibility.PUBLIC
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.kotlinFunction

internal data class ScannedPackageContext<F>(
    val packageName: String,
    private val contextFuns: List<KFunction0<TopLevelContextBuilder<F>>>,
    override val properties: Map<Any, Any> = emptyMap()

) : RuntimeContext<Unit, F>() {
    override fun runTest(test: Test<F>, parentFixture: Unit, testDescriptor: TestDescriptor) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override val name: String get() = packageName
    override val children: List<RuntimeNode<F, *>> by lazy {
        contextFuns.map { f ->
            val rootWithDefaultName: RuntimeContext<Unit, F> = f().buildNode()
            RuntimeContextWrapper(delegate = rootWithDefaultName, name = f.name) as RuntimeNode<F, *>
        }
    }
    
    override fun withChildren(children: List<RuntimeNode<F, *>>): RuntimeContext<Unit, F> {
        return RuntimeContextWrapper(name, emptyMap(), children, {_, _,_ -> Unit}, {})
    }
    
    override fun close() {}
}

internal fun scan(scannerConfig: ClassGraph.() -> Unit, classFilter: (ClassInfo) -> Boolean = {true}): List<ScannedPackageContext<*>> {
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
        .map { (packageName, functions) -> ScannedPackageContext(packageName, functions as List<KFunction0<TopLevelContextBuilder<Any>>>) }
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

