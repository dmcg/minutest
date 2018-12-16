package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.*
import io.github.classgraph.*
import kotlin.reflect.KFunction0
import kotlin.reflect.KVisibility.PUBLIC
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.kotlinFunction

internal data class ScannedPackageContext(
    val packageName: String,
    private val contextFuns: List<KFunction0<NodeBuilder<Unit, *>>>,
    override val properties: Map<Any, Any> = emptyMap()

) : RuntimeContext<Unit>() {

    override val parent = null
    override val name: String get() = packageName
    override val children: List<RuntimeNode> by lazy {
        contextFuns.map { f ->
            val rootWithDefaultName = f().buildRootNode()
            when (rootWithDefaultName) {
                is RuntimeContext<*> -> RuntimeContextWrapper(rootWithDefaultName,
                    name = f.name)
                is RuntimeTest -> RuntimeTestWrapper(delegate = rootWithDefaultName,
                    name = f.name)
            }
        }
    }

    override fun adoptedBy(parent: RuntimeContext<*>?) = TODO("not implemented")
    override fun runTest(test: Test<Unit>) = TODO("not implemented")
    override fun withChildren(children: List<RuntimeNode>) = TODO("not implemented")
    
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

private fun MethodInfo.toKotlinFunction(): KFunction0<NodeBuilder<Unit, *>>? {
    @Suppress("UNCHECKED_CAST")
    return loadClassAndGetMethod().kotlinFunction as? KFunction0<NodeBuilder<Unit, *>>
}

private fun MethodInfo.definesTopLevelContext() =
    isStatic && isPublic && parameterInfo.isEmpty() && !isBridge
        && typeSignatureOrTypeDescriptor.resultType.name() == NodeBuilder::class.java.name

private fun TypeSignature.name() =
    (this as? ClassRefTypeSignature)?.baseClassName


// another way to look at it

internal fun rootFor(packageName: String, contextFuns: List<KFunction0<NodeBuilder<Unit, *>>>): RuntimeContext<Unit> {
    val rootNodes: List<RuntimeNode> = contextFuns.map { it().buildRootNode() }
        .map { when (it) {
            is RuntimeTest -> listOf(it)
            is RuntimeContext<*> -> it.children
        } }
        .flatten()
    // Each of the childBuilders is building a root context. We need to take children out of these and put them
    // into a new root
    val rootNodesAsNodeBuilders: List<NodeBuilder<Unit,*>> = rootNodes.map { it.asNodeBuilder<Unit>() }

    return PreparedRuntimeContext<Unit, Unit>(
        name = packageName,
        parent = null,
        fixtureFactory = { _, _ -> Unit },
        childBuilders = rootNodesAsNodeBuilders
    )
}

private fun <F> RuntimeNode.asNodeBuilder(): NodeBuilder<Unit, *> = object: NodeBuilder<Unit, F> {
    override val properties = this@asNodeBuilder.properties.toMutableMap()
    override fun buildNode(parent: RuntimeContext<Unit>?) = this@asNodeBuilder.adoptedBy(parent)
}