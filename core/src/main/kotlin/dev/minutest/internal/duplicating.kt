package dev.minutest.internal

import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty1
import kotlin.reflect.full.createType
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberFunctions

/**
 * Returns a function that will duplicate a data class changing a specific property.
 */
@Suppress("UNCHECKED_CAST")
internal fun <T : Any, V> duplicatorFor(
    property: KProperty1<T, V>
): (T, V) -> T {
    val kClass = property.instanceParameter?.type?.classifier as? KClass<T>
        ?: error("Not a property on a class")
    val copyFunction = kClass.memberFunctions.find {
        it.name == "copy" && it.returnType == kClass.createType()
    } as? KFunction<T> ?: error("No copy method")
    val instanceParameter = copyFunction.instanceParameter
        ?: error("copy is static")
    val propertyParameter = copyFunction.parameters.find {
        it.name == property.name && it.type.isSubtypeOf(property.returnType)
    } ?: error("Can't find a copy parameter named ${property.name} with type ${property.returnType}")
    return { thing, value ->
        copyFunction.callBy(
            mapOf(
                instanceParameter to thing,
                propertyParameter to value
            )
        )
    }
}