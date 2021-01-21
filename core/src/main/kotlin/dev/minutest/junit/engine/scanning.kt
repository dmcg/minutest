package dev.minutest.junit.engine

import dev.minutest.Node
import dev.minutest.RootContextBuilder
import dev.minutest.internal.AmalgamatedRootContext
import dev.minutest.internal.rootContextFromMethods
import io.github.classgraph.*
import kotlin.reflect.KFunction0
import kotlin.reflect.KVisibility.PUBLIC
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.kotlinFunction

internal fun scanForRootNodes(
    scannerConfig: ClassGraph.() -> Unit,
    classFilter: (ClassInfo) -> Boolean = { true }
) : List<Node<Unit>> {
    val methodInfos: Sequence<MethodInfo> = ClassGraph()
        .enableClassInfo()
        .enableMethodInfo()
        .enableAnnotationInfo()
        .disableJarScanning()
        .disableNestedJarScanning()
        .apply(scannerConfig)
        .scan()
        .allClasses
        .asSequence()
        .filter { it.isStandardClass && !it.isAnonymousInnerClass && it.isPublic && !it.isSynthetic }
        .filter(classFilter)
        .flatMap { it.declaredMethodInfo.asSequence() }

    val (methods, functions) = methodInfos
        .filter { it.definesARootContext() }
        .partition { !it.isStatic }

    val methodContexts: List<Node<Unit>> = methods
        .mapNotNull { it.toKotlinFunction()?.javaMethod?.declaringClass }
        .toSet()
        .map { it.constructors.single().newInstance().rootContextFromMethods() }

    val topLevelContexts = functions
        .mapNotNull { it.toKotlinFunction() }
        // Check Kotlin visibility because a public static Java method might have internal visibility in Kotlin
        .filter { it.visibility == PUBLIC }
        .groupBy { it.javaMethod?.declaringClass?.`package`?.name ?: "<tests>" }
        .map { (packageName, functions: List<RootContextFun>) ->
            AmalgamatedRootContext(packageName, functions.renamed().map { it.buildNode() })
        }
    return methodContexts + topLevelContexts
}

@Suppress("UNCHECKED_CAST") // reflection
private fun MethodInfo.toKotlinFunction(): RootContextFun? =
    loadClassAndGetMethod().kotlinFunction as? RootContextFun

private fun MethodInfo.definesARootContext() =
    isPublic && parameterInfo.isEmpty() && !isBridge &&
        typeSignatureOrTypeDescriptor.resultType.name() == RootContextBuilder::class.java.name
        && hasAnnotation("org.junit.platform.commons.annotation.Testable")

private fun TypeSignature.name() = (this as? ClassRefTypeSignature)?.baseClassName

private fun Iterable<RootContextFun>.renamed(): List<RootContextBuilder> =
    this.map { f: RootContextFun ->
        f().withNameUnlessSpecified(f.name)
    }

private typealias RootContextFun = KFunction0<RootContextBuilder>