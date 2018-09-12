package com.oneeyedmen.minutest

import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicContainer.dynamicContainer
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.jvm.javaType

annotation class Minutest

interface Minutests {

    @TestFactory fun test() = this::class.testMethods().map { method ->
        val returnType = method.returnType
        when {
            returnType.isSubtypeOf(KFunction::class.starProjectedType)-> dynamicContainerForKFunctionResultOf(method)
            returnType.isSubtypeOf(Function0::class.starProjectedType) -> dynamicContainerForFunction0ResultOf(method)
            returnType.isSubtypeOf(Function::class.starProjectedType) -> error("Hmmm, still thinking about this")
            else -> dynamicTestFor(method)
        }
    }

    private fun dynamicContainerForKFunctionResultOf(method: KFunction<*>): DynamicContainer {
        val function = method.call(this) as KFunction<*>
        return dynamicContainer(method.name, listOf(dynamicTestFor(function)))
    }

    private fun dynamicTestFor(callable: KCallable<*>) = dynamicTest(callable.name) {
        val parameterCount = callable.parameters.size
        when  {
            parameterCount == 0 -> callable.call()
            parameterCount == 1 && callable.parameters[0].type.javaType == this::class.java-> callable.call(this)
            parameterCount == 1 -> callable.call((callable.parameters[0].type.javaType as Class<*>).newInstance())
            parameterCount == 2 -> callable.call(this, (callable.parameters[1].type.javaType as Class<*>).newInstance())
            else -> error("Only one state parameter accepted")
        }
    }

    private fun dynamicContainerForFunction0ResultOf(method: KFunction<*>): DynamicContainer {
        val function = method.call(this) as () -> Function0<*>
        return dynamicContainer(method.name, listOf(dynamicTestFor(function)))
    }

    private fun dynamicTestFor(f: () -> Any?) = dynamicTest(f.toString()) {
        f()
    }
    private fun KClass<*>.testMethods() = this.memberFunctions.filter { it.hasTestAnnotation }

    private val KFunction<*>.hasTestAnnotation: Boolean get() = findAnnotation<Minutest>() != null

}

