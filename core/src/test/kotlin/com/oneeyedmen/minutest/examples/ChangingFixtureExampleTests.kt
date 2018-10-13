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

        translatingContext<Fixture1, Fixture2>("inner") {

            translateFixture<Fixture1, Fixture2> {
                Fixture2(fruit, "smoothie")
            }

            test("takes Fixture 2") {
                assertEquals("banana", fruit)
                assertEquals("smoothie", thing)
            }
        }
    }
}

inline fun <F: Any, reified F2 : Any> TestContext<F>.translatingContext(
    name: String,
    noinline builder: TestContext<F2>.() -> Unit
) = this.translatingContext(name, F2::class, builder)

@Suppress("UNCHECKED_CAST")
fun <F: Any, F2 : Any> TestContext<F>.translatingContext(name: String, newFixtureType: KClass<F2>, builder: TestContext<F2>.() -> Unit): TestContext<F2> {
    val parent = this as MiContext<F>
    return MiContext(name, newFixtureType, builder).also {
        parent.children.add(it as Node<F>)
    }
}

@Suppress("UNCHECKED_CAST")
fun <F: Any, F2: Any> TestContext<F2>.translateFixture(converter: F.() -> F2) {
    this.replaceFixture { converter(this as F) }
}
