package dev.minutest.junit.engine

import dev.minutest.RootContextBuilder
import dev.minutest.internal.AmalgamatedRootContext
import dev.minutest.internal.lazyRootRootContext
import dev.minutest.internal.rootContextForClass
import dev.minutest.internal.time
import io.github.classgraph.*
import kotlin.reflect.KClass
import kotlin.reflect.KFunction0
import kotlin.reflect.KVisibility.PUBLIC
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.kotlinFunction

internal fun scanForRootNodes(
    scannerConfig: ClassGraph.() -> Unit,
    classFilter: (ClassInfo) -> Boolean = { true }
): List<AmalgamatedRootContext> =
    time("ClassGraph scanning ") {
        classGraphWith(scannerConfig)
            .scan()
            .use { scanResult ->
                val methodInfos: Sequence<MethodInfo> = scanResult
                    .allClasses
                    .asSequence()
                    .filter {
                        classFilter(it) &&
                            it.isStandardClass && !it.isAnonymousInnerClass && it.isPublic && !it.isSynthetic &&
                            it.hasMethodAnnotation("org.junit.platform.commons.annotation.Testable")
                    }
                    .flatMap { it.declaredMethodInfo.asSequence() }

                val (methods, functions) = methodInfos
                    .filter { it.definesARootContext() }
                    .partition { !it.isStatic }

                val methodContexts = methods
                    .mapNotNull { it.toKotlinFunction()?.javaMethod?.declaringClass }
                    .toSet()
                    .mapNotNull { rootContextForClass(it.kotlin,) }

                val topLevelContexts = functions
                    .mapNotNull { it.toKotlinFunction() }
                    // Check Kotlin visibility because a public static Java method might have internal visibility in Kotlin
                    .filter { it.visibility == PUBLIC }
                    .groupBy { it.javaMethod?.declaringClass?.`package`?.name ?: "<tests>" }
                    .map { (packageName, functions: List<RootContextFun>) ->
                        lazyRootRootContext(packageName, functions) {
                            emptyArray()
                        }
                    }
                (methodContexts + topLevelContexts)
            }
    }

private fun classGraphWith(scannerConfig: ClassGraph.() -> Unit) =
    ClassGraph()
        .enableClassInfo()
        .enableMethodInfo()
        .enableAnnotationInfo()
        .disableJarScanning()
        .disableNestedJarScanning()
        .apply(scannerConfig)

@Suppress("UNCHECKED_CAST") // reflection
private fun MethodInfo.toKotlinFunction(): RootContextFun? =
    loadClassAndGetMethod().kotlinFunction as? RootContextFun

private fun MethodInfo.definesARootContext() =
    isPublic && parameterInfo.isEmpty()
        && !isBridge &&
        typeSignatureOrTypeDescriptor.resultType.isLike(RootContextBuilder::class)
        && hasAnnotation("org.junit.platform.commons.annotation.Testable")

private fun TypeSignature.isLike(kClass: KClass<*>) =
    name() == kClass.java.name

private fun TypeSignature.name() = (this as? ClassRefTypeSignature)?.baseClassName

private typealias RootContextFun = KFunction0<RootContextBuilder>