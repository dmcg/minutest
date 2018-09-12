package com.oneeyedmen.minutest

import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicContainer.dynamicContainer
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import java.lang.reflect.Method
import kotlin.reflect.KCallable
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaType

annotation class Minutest

interface Minutests {

    @TestFactory fun test() = this::class.java.testMethods().map { method ->
        val returnType = method.returnType.also { println(it) }
        when {
            KFunction::class.java.isAssignableFrom(returnType) -> dynamicContainerForKFunctionResultOf(method)
            Function0::class.java.isAssignableFrom(returnType) -> dynamicContainerForFunction0ResultOf(method)
            Function::class.java.isAssignableFrom(returnType) -> error("Hmmm, still thinking about this")
            else -> dynamicTestFor(method)
        }
    }

    private fun dynamicContainerForKFunctionResultOf(method: Method): DynamicContainer {
        val function = method.invoke(this) as KFunction<*>
        return dynamicContainer(method.name, listOf(dynamicTestFor(function)))
    }

    private fun dynamicContainerForFunction0ResultOf(method: Method): DynamicContainer {
        val function = method.invoke(this) as () -> Function0<*>
        return dynamicContainer(method.name, listOf(dynamicTestFor(function)))
    }

    private fun dynamicTestFor(method: Method) = dynamicTest(method.name) {
        val parameterCount = method.parameterCount
        when (parameterCount) {
            0 -> method.invoke(this)
            1 -> method.invoke(this, method.parameters.first().type.newInstance())
            else -> error("Only one state parameter accepted")
        }
    }

    private fun dynamicTestFor(callable: KCallable<*>) = dynamicTest(callable.name) {
        val parameterCount = callable.parameters.size
        when (parameterCount) {
            0 -> callable.call()
            1 -> callable.call((callable.parameters.first().type.javaType as Class<*>).newInstance())
            else -> error("Only one state parameter accepted")
        }
    }

    private fun dynamicTestFor(f: () -> Any?) = dynamicTest(f.toString()) {
        f()
    }

    private fun Class<*>.testMethods() = methods.filter { it.hasTestAnnotation }

    private val Method.hasTestAnnotation: Boolean get() = getAnnotation(Minutest::class.java) != null

}

