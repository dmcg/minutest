package dev.minutest.internal

import dev.minutest.*
import dev.minutest.experimental.TestAnnotation
import io.github.classgraph.*
import kotlin.reflect.KFunction0
import kotlin.reflect.KVisibility.PUBLIC
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.kotlinFunction

internal data class ScannedPackageContext(
    val packageName: String,
    private val contextBuilderBuilders: List<() -> RootContextBuilder<*>>,
    override val annotations: List<TestAnnotation> = emptyList()
) : Context<Unit, Unit>() {

    override val name: String get() = packageName

    override val children: List<Node<Unit>> by lazy {
        contextBuilderBuilders.map { f ->
            f().buildNode()
        }
    }

    override fun runTest(testlet: Testlet<Unit>, parentFixture: Unit, testDescriptor: TestDescriptor) =
        RootExecutor.runTest(testlet, testDescriptor)

    override fun withTransformedChildren(transform: NodeTransform) = TODO()

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
        .map { (packageName, functions) -> ScannedPackageContext(packageName, functions.renamed()) }
}

private fun MethodInfo.toKotlinFunction(): KFunction0<RootContextBuilder<*>>? {
    @Suppress("UNCHECKED_CAST")
    return loadClassAndGetMethod().kotlinFunction as? KFunction0<RootContextBuilder<*>>
}

private fun MethodInfo.definesTopLevelContext() =
    isStatic && isPublic && parameterInfo.isEmpty() && !isBridge
        && typeSignatureOrTypeDescriptor.resultType.name() == RootContextBuilder::class.java.name

private fun TypeSignature.name() =
    (this as? ClassRefTypeSignature)?.baseClassName

internal fun Iterable<KFunction0<RootContextBuilder<*>>>.renamed(): List<() -> RootContextBuilder<*>> =
    this.map { f -> { RenamedRootContextBuilder(f(), f.name) } }