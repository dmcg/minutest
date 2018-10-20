package com.oneeyedmen.minutest.junit

import com.oneeyedmen.minutest.TestContext
import com.oneeyedmen.minutest.internal.asKType
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.TestFactory
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl
import java.lang.reflect.ParameterizedType
import java.util.stream.Stream
import kotlin.reflect.KClass

/**
 * EXPERIMENTAL Base class for tests that you want run with JUnit 5
 */
abstract class JUnitTests<F>(
    override val tests: TestContext<F>.() -> Unit,
    override val fixtureIsNullable: Boolean = false
) : IRunTestsInJUnit5<F>

abstract class JUnitFixtureTests<F>(
    override val fixtureIsNullable: Boolean = false
) : IRunTestsInJUnit5<F> {

    // little thunk to prevent having to specify the type of the tests val
    protected fun tests(context: TestContext<F>.() -> Unit) = context
}

interface IRunTestsInJUnit5<F> {
    val tests: TestContext<F>.() -> Unit
    val fixtureIsNullable: Boolean

    @Suppress("UNCHECKED_CAST")
    private fun myGenericFixtureType(): KClass<*> {
        val parameterizedType = this::class.java.genericSuperclass as ParameterizedType
        val genericType = parameterizedType.actualTypeArguments[0]
        return when (genericType) {
            is Class<*> -> genericType.kotlin
            is ParameterizedTypeImpl -> genericType.rawType.kotlin
            else -> error("Unexpected fixture type")
        }
    }

    @TestFactory
    fun tests(): Stream<out DynamicNode> = junitTests(myGenericFixtureType().asKType(fixtureIsNullable), tests)
}