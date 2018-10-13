package com.oneeyedmen.minutest.examples

import com.oneeyedmen.minutest.TestContext
import com.oneeyedmen.minutest.internal.MiContext
import com.oneeyedmen.minutest.internal.Node
import com.oneeyedmen.minutest.junit.junitTests
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.TestFactory
import kotlin.reflect.KClass


object ChangingFixtureExampleTests {

    data class Fixture1(val fruit: String)

    data class Fixture2(val fruit: String, val thing: String)

    @TestFactory fun test() = junitTests<Fixture1> {

        fixture { Fixture1("banana") }

        test("takes Fixture1") {
            assertEquals("banana", fruit)
        }

        context("inner", converter = { Fixture2(fruit, "smoothie") } ) {

            test("takes Fixture 2") {
                assertEquals("banana", fruit)
                assertEquals("smoothie", thing)
            }
        }
    }
}

inline fun <F: Any, reified F2 : Any> TestContext<F>.context(
    name: String,
    noinline converter: F.() -> F2,
    noinline builder: TestContext<F2>.() -> Unit
) = this.translatingContext(name, F2::class, converter, builder)

@Suppress("UNCHECKED_CAST")
fun <F: Any, F2 : Any> TestContext<F>.translatingContext(name: String, newFixtureType: KClass<F2>, converter: F.() -> F2, builder: TestContext<F2>.() -> Unit): TestContext<F2> {
    val parent = this as MiContext<F>
    val translatingContext: MiContext<F2> = MiContext(name, newFixtureType, builder)
    translatingContext.replaceFixture { converter(this as F) }
    parent.children.add(translatingContext as Node<F>)
    return translatingContext
}

