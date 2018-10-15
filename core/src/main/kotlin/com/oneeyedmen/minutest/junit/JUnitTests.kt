package com.oneeyedmen.minutest.junit

import com.oneeyedmen.minutest.TestContext
import org.junit.jupiter.api.TestFactory
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl
import java.lang.reflect.ParameterizedType
import kotlin.reflect.KClass

/**
 * EXPERIMENTAL Base class for tests that you want run with JUnit 5
 */
abstract class JUnitTests<F : Any>(private val block: TestContext<F>.() -> Unit) {

    @Suppress("UNCHECKED_CAST")
    private fun myGenericFixtureType(): KClass<F> {
        val parameterizedType = this::class.java.genericSuperclass as ParameterizedType
        val genericType = parameterizedType.actualTypeArguments[0]
        return when (genericType) {
            is Class<*> -> genericType.kotlin as KClass<F>
            is ParameterizedTypeImpl -> genericType.rawType.kotlin as KClass<F>
            else -> error("Unexpected fixture type")
        }
    }

    @TestFactory
    fun tests() = junitTests(myGenericFixtureType(), block)
}