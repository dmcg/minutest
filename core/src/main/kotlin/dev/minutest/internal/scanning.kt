package dev.minutest.internal

import dev.minutest.Node
import dev.minutest.RootContextBuilder
import io.github.classgraph.*
import kotlin.reflect.KFunction0
import kotlin.reflect.KVisibility.PUBLIC
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.kotlinFunction

internal fun findRootContextPerPackage(
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

    val classBasedOnes: List<Node<Unit>> = methodInfos
        .filter { it.definesMethodContext() }
        .mapNotNull { it.toKotlinFunction()?.javaMethod?.declaringClass }
        .toSet()
        .map { it.constructors.single().newInstance().rootContextFromMethods() }

    val topLevelOnes = methodInfos
        .filter { it.definesTopLevelContext() }
        .mapNotNull { it.toKotlinFunction() }
        // Check Kotlin visibility because a public static Java method might have internal visibility in Kotlin
        .filter { it.visibility == PUBLIC }
        .groupBy { it.javaMethod?.declaringClass?.`package`?.name ?: "<tests>" }
        .map { (packageName, functions: List<RootContextFun>) ->
            AmalgamatedRootContext(packageName, functions.renamed().map { it.buildNode() })
        }
    return classBasedOnes + topLevelOnes
}

@Suppress("UNCHECKED_CAST") // reflection
private fun MethodInfo.toKotlinFunction(): RootContextFun? =
    loadClassAndGetMethod().kotlinFunction as? RootContextFun

private fun MethodInfo.definesTopLevelContext() =
    isStatic && isPublic && parameterInfo.isEmpty() && !isBridge &&
        typeSignatureOrTypeDescriptor.resultType.name() == RootContextBuilder::class.java.name

private fun MethodInfo.definesMethodContext() =
    isPublic && parameterInfo.isEmpty() && !isBridge &&
        typeSignatureOrTypeDescriptor.resultType.name() == RootContextBuilder::class.java.name
        && hasAnnotation("org.junit.platform.commons.annotation.Testable")

private fun TypeSignature.name() = (this as? ClassRefTypeSignature)?.baseClassName

private fun Iterable<RootContextFun>.renamed(): List<RootContextBuilder> =
    this.map { f: RootContextFun ->
        f().withNameUnlessSpecified(f.name)
    }

private typealias RootContextFun = KFunction0<RootContextBuilder>