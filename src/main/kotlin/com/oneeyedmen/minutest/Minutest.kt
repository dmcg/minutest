package com.oneeyedmen.minutest

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DynamicContainer.dynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.jvm.javaType
import kotlin.streams.asStream

annotation class Minutest

interface Minutests {

    @TestFactory fun minutests() = this::class.testMethods().map { callable: KCallable<*> ->
        dynamicNodeFor(callable)
    }

    @Suppress("UNCHECKED_CAST")
    private fun dynamicNodeFor(method: KCallable<*>): DynamicNode {
        val returnType: KType = method.returnType
        return when {
            returnType.isSubtypeOf(Function::class.starProjectedType) ->
                dynamicContainerFor(returnType, method)
            returnType.isA<Iterable<*>>(of = KCallable::class) ->
                dynamicContainer(method.name, (method.call(this) as Iterable<KCallable<*>>).map { dynamicNodeFor(it) } )
            returnType.isA<Sequence<*>>(of = KCallable::class) ->
                dynamicContainer(method.name, (method.call(this) as Sequence<KCallable<*>>).map { dynamicNodeFor(it) }.asStream() )
            returnType.isA<Iterable<*>>(of = NamedFunction::class) ->
                dynamicContainer(method.name, (method.call(this) as Iterable<NamedFunction>).map { dynamicNodeFor(it) } )
            returnType.isA<Sequence<*>>(of = NamedFunction::class) ->
                dynamicContainer(method.name, (method.call(this) as Sequence<NamedFunction>).map { dynamicNodeFor(it) }.asStream() )
            else -> dynamicTestFor(method)
        }
    }


    @Suppress("UNCHECKED_CAST")
    private fun dynamicContainerFor(returnType: KType, method: KCallable<*>) = when {
        returnType.isSubtypeOf(KFunction::class.starProjectedType) ->
            dynamicContainer(method.name, listOf(dynamicTestFor(method.call(this) as KFunction<*>)))
        returnType.isSubtypeOf(Function0::class.starProjectedType) ->
            dynamicContainer(method.name, listOf(dynamicTestFor(method.call(this) as () -> Function0<*>)))
        else -> error("Hmmm, still thinking about this")
    }

    private fun dynamicNodeFor(namedFunction: NamedFunction) = dynamicTest(namedFunction.name) {
        namedFunction.f()
    }

    private fun dynamicTestFor(callable: KCallable<*>) = dynamicTest(callable.name) {
        val parameterCount = callable.parameters.size
        when {
            parameterCount == 0 -> callable.call()
            parameterCount == 1 && callable.parameters[0].type.javaType == this::class.java -> callable.call(this)
            parameterCount == 1 -> callable.call((callable.parameters[0].type.javaType as Class<*>).newInstance())
            parameterCount == 2 -> callable.call(this, (callable.parameters[1].type.javaType as Class<*>).newInstance())
            else -> error("Only one state parameter accepted")
        }
    }

    private fun dynamicTestFor(f: () -> Any?) = dynamicTest(f.toString()) {
        f()
    }

    private fun KClass<*>.testMethods(): List<KCallable<*>> = this.memberFunctions.filter {
        it.findAnnotation<Minutest>() != null && it.findAnnotation<Disabled>() == null
    }

}

inline fun <reified T> KType.isA(of: KClass<*>) =
    isSubtypeOf(T::class.starProjectedType) && arguments[0].type!!.isSubtypeOf(of.starProjectedType)

operator fun String.invoke(f: () -> Any) = NamedFunction(this, f)

data class NamedFunction(val name: String, val f: () -> Any)

