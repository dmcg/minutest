package com.oneeyedmen.minutest.junit

import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl
import java.lang.reflect.ParameterizedType
import kotlin.reflect.KClass

internal interface IKnowMyGenericClass<F> {

    @Suppress("UNCHECKED_CAST")
    fun myGenericClass(): KClass<*> {
        val parameterizedType = this::class.java.genericSuperclass as ParameterizedType
        val genericType = parameterizedType.actualTypeArguments[0]
        return when (genericType) {
            is Class<*> -> genericType.kotlin
            is ParameterizedTypeImpl -> genericType.rawType.kotlin
            else -> error("Unexpected fixture type")
        }
    }
}