# [Minutest](README.md)

## Fixtures

The [JUnit 4 Wiki](https://github.com/junit-team/junit4/wiki/test-fixtures) says:

> A test fixture is a fixed state of a set of objects used as a baseline for running tests. The purpose of a test fixture is to ensure that there is a well known and fixed environment in which tests are run so that results are repeatable.

Here are some ways of using fixtures in Minutest.

### No Fixture

You don't have to have a fixture. The simplest tests can just have assertions.

```kotlin
class NoFixtureExampleTests : JupiterTests {

    override val tests = context<Unit> {

        context("addition") {
            test("positive + positive") {
                assertEquals(4, 3 + 1)
            }
            test("positive + negative") {
                assertEquals(2, 3 + -1)
            }
        }
        context("subtraction") {
            test("positive - positive") {
                assertEquals(2, 3 - 1)
            }
            test("positive - negative") {
                assertEquals(4, 3 - -1)
            }
        }
    }
}
```

### Subject Under Test as Fixture

It makes sense to have the subject under test as the fixture if it has the only state in the test.

```kotlin
class SubjectUnderTestFixtureExampleTests : JupiterTests {

    override val tests = context<List<String>> {

        context("empty") {
            fixture {
                emptyList()
            }
            test("is empty") {
                // when the fixture is the subject, 'it' reads well
                assertTrue(it.isEmpty())
            }
            test("no head") {
                assertNull(it.firstOrNull())
            }
        }

        // Note that the context name and the fixture state agree
        context("not empty") {
            fixture {
                listOf("item")
            }
            test("is not empty") {
                assertFalse(it.isEmpty())
            }
            test("has head") {
                assertEquals("item", it.firstOrNull())
            }
        }
    }
}
```

Even if the subject is immutable then you can [inspect it in after blocks](immutable-fixtures.md).
 
### Arguments as Fixture

If you are testing static functions, making the arguments the fixture can be expressive. 

```kotlin
class ArgumentsAsFixtureExampleTests : JupiterTests {

    data class Arguments(val l: Int, val r: Int)

    override val tests = context<Arguments> {

        context("positive positive") {
            fixture {
                Arguments(l = 3, r = 1)
            }
            test("addition") {
                assertEquals(4, l + r)
            }
            test("subtraction") {
                assertEquals(2, l - r)
            }
        }

        context("positive negative") {
            fixture {
                Arguments(l = 3, r = -1)
            }
            test("addition") {
                assertEquals(2, l + r)
            }
            test("subtraction") {
                assertEquals(4, l - r)
            }
        }
    }
}
```

Again, where possible having the context name expressed in the fixture state, and vice-versa, keeps things honest.

### Compound Fixture

When testing a system that mediates between other components, it makes sense to bring them all into the fixture - this gives the test isolation and repeatability that is the point of the fixture.

```kotlin
class ControlPanel(
    val keySwitch: () -> Boolean,
    val beep: () -> Unit,
    val launchMissile: () -> Unit
) {
    fun pressButton() {
        if (keySwitch())
            launchMissile()
        else
            beep()
    }
    val warningLight get() = keySwitch()
}

class CompoundFixtureExampleTests : JupiterTests {

    class Fixture() {
        // Rather than introduce a mocking framework, we can work with
        // functions and mutable state.
        var keySwitchOn = false
        var beeped = false
        var missileLaunched = false

        val controlPanel = ControlPanel(
            keySwitch = { keySwitchOn },
            beep = { beeped = true },
            launchMissile = { missileLaunched = true }
        )
    }

    override val tests = context<Fixture> {
        fixture { Fixture() }

        context("key not turned") {
            test("light off") {
                assertFalse(controlPanel.warningLight)
            }
            test("cannot launch") {
                controlPanel.pressButton()
                assertTrue(beeped)
                assertFalse(missileLaunched)
            }
        }

        context("key turned") {
            modifyFixture {
                keySwitchOn = true
            }
            test("light on") {
                assertTrue(controlPanel.warningLight)
            }
            test("will launch") {
                controlPanel.pressButton()
                assertFalse(beeped)
                assertTrue(missileLaunched)
            }
        }
    }
}
```

### Parent Fixtures

Fixtures are inherited from the parent context, and may be replaced or modified by children.

```kotlin
class ParentFixtureExampleTests : JupiterTests {

    data class Fixture(var fruit: String)

    override val tests = context<Fixture> {
        fixture {
            Fixture("banana")
        }

        test("sees the context's fixture") {
            assertEquals("banana", fruit)
        }

        context("context inherits fixture") {
            test("sees the parent context's fixture") {
                assertEquals("banana", fruit)
            }
        }

        context("context replaces fixture") {
            fixture {
                Fixture("kumquat")
            }
            test("sees the replaced fixture") {
                assertEquals("kumquat", fruit)
            }
        }

        context("context modifies fixture") {
            modifyFixture {
                fruit = "apple"
            }
            test("sees the modified fixture") {
                assertEquals("apple", fruit)
            }
        }
    }
}
```

### Changing Fixture Type

A context may change the type of its parent fixture.

```kotlin
class DerivedContextExampleTests : JupiterTests {

    data class Fruit(val name: String)

    data class FruitDrink(val fruit: Fruit, val name: String) {
        override fun toString() = "${fruit.name} $name"
    }

    override val tests = context<Fruit> {

        fixture {
            Fruit("banana")
        }

        test("takes Fixture") {
            assertEquals("banana", name)
        }

        // To change fixture type use derivedContext
        derivedContext<FruitDrink>("change in fixture type") {

            // We have to specify how to convert a Fruit to a FruitDrink
            deriveFixture {
                FruitDrink(parentFixture, "smoothie")
            }

            test("takes FruitDrink") {
                assertEquals("banana smoothie", this.toString())
            }
        }
    }
}
```

