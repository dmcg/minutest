[Minutest](README.md)

# Fixtures

## What is a test fixture?

[Wikipedia](https://en.wikipedia.org/wiki/Test_fixture) says:

> A test fixture is something used to consistently test some item, device, or piece of software. Test fixtures can be found when testing electronics, software and physical devices.

The [JUnit 4 Wiki](https://github.com/junit-team/junit4/wiki/test-fixtures) says:

> A test fixture is a fixed state of a set of objects used as a baseline for running tests. The purpose of a test fixture is to ensure that there is a well known and fixed environment in which tests are run so that results are repeatable.

So a test fixture is something to give us consistency / repeatability. In software we try where possible to create a fixture that encapsulates all the state that can affect the result of running the test, or is affected by the running of the test. That way, by creating a fresh fixture for each test, we can prevent one test run from affecting a later one.

There is a notable difference between a physical test fixture and a software test fixture. When testing a physical part it is mounted *in* the fixture - they are separate. When testing a software object, then the fixture will generally create that object - the subject under test will be a property of the fixture. This isn't a hard and fast rule though; in particular when testing stateless objects, stand-alone functions, or external services.

Here are some ways of using fixtures in Minutest.

## No Fixture

You don't have to have a fixture. The simplest tests can just have assertions.

[start-insert]: <../core/src/test/kotlin/dev/minutest/examples/fixtures/NoFixtureExampleTests.kt>
```kotlin
class NoFixtureExampleTests : JUnit5Minutests {

    fun tests() = rootContext {

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
<small>[../core/src/test/kotlin/dev/minutest/examples/fixtures/NoFixtureExampleTests.kt](../core/src/test/kotlin/dev/minutest/examples/fixtures/NoFixtureExampleTests.kt)</small>

[end-insert]: <>

## Subject Under Test as Fixture

It makes sense to have the subject under test as the fixture if it has the only state in the test.

[start-insert]: <../core/src/test/kotlin/dev/minutest/examples/fixtures/SubjectUnderTestFixtureExampleTests.kt>
```kotlin
class SubjectUnderTestFixtureExampleTests : JUnit5Minutests {

    fun tests() = rootContext<List<String>> {

        context("empty") {
            fixture {
                emptyList()
            }
            test("is empty") {
                assertTrue(fixture.isEmpty())
            }
            test("no head") {
                assertNull(fixture.firstOrNull())
            }
        }

        // Note that the context name and the fixture state agree
        context("not empty") {
            fixture {
                listOf("item")
            }
            test("is not empty") {
                assertFalse(fixture.isEmpty())
            }
            test("has head") {
                assertEquals("item", fixture.firstOrNull())
            }
        }
    }
}
```
<small>[../core/src/test/kotlin/dev/minutest/examples/fixtures/SubjectUnderTestFixtureExampleTests.kt](../core/src/test/kotlin/dev/minutest/examples/fixtures/SubjectUnderTestFixtureExampleTests.kt)</small>

[end-insert]: <>

Even if the subject is immutable then you can [inspect it in after blocks](immutable-fixtures.md).
 
## Arguments as Fixture

If you are testing static functions, making the arguments the fixture can be expressive. 

[start-insert]: <../core/src/test/kotlin/dev/minutest/examples/fixtures/ArgumentsAsFixtureExampleTests.kt>
```kotlin
class ArgumentsAsFixtureExampleTests : JUnit5Minutests {

    data class Arguments(val l: Int, val r: Int)

    fun tests() = rootContext<Arguments> {

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
<small>[../core/src/test/kotlin/dev/minutest/examples/fixtures/ArgumentsAsFixtureExampleTests.kt](../core/src/test/kotlin/dev/minutest/examples/fixtures/ArgumentsAsFixtureExampleTests.kt)</small>

[end-insert]: <>

Again, where possible having the context name expressed in the fixture state, and vice-versa, keeps things honest.

## Compound Fixture

When testing a system that mediates between other components, it makes sense to bring them all into the fixture - this gives the test isolation and repeatability that is the point of the fixture.

[start-insert]: <../core/src/test/kotlin/dev/minutest/examples/fixtures/CompoundFixtureExampleTests.kt>
```kotlin
class ControlPanel(
    private val beep: () -> Unit,
    private val launchRocket: () -> Unit
) {
    private var keyTurned: Boolean = false

    fun turnKey() {
        keyTurned = true
    }

    fun pressButton() {
        if (keyTurned)
            launchRocket()
        else
            beep()
    }
    val warningLightOn get() = keyTurned
}

class CompoundFixtureExampleTests : JUnit5Minutests {

    // The fixture consists of all the state affected by tests
    class Fixture() {
        var beeped = false
        var launched = false

        val controlPanel = ControlPanel(
            beep = { beeped = true },
            launchRocket = { launched = true }
        )
    }

    fun tests() = rootContext<Fixture> {
        fixture { Fixture() }

        context("key not turned") {
            test("light is off") {
                assertFalse(controlPanel.warningLightOn)
            }
            test("cannot launch when pressing button") {
                controlPanel.pressButton()
                assertTrue(beeped)
                assertFalse(launched)
            }
        }

        context("key turned") {
            modifyFixture {
                controlPanel.turnKey()
            }
            test("light is on") {
                assertTrue(controlPanel.warningLightOn)
            }
            test("launches when pressing button") {
                controlPanel.pressButton()
                assertFalse(beeped)
                assertTrue(launched)
            }
        }
    }
}
```
<small>[../core/src/test/kotlin/dev/minutest/examples/fixtures/CompoundFixtureExampleTests.kt](../core/src/test/kotlin/dev/minutest/examples/fixtures/CompoundFixtureExampleTests.kt)</small>

[end-insert]: <>

## Parent Fixtures

Fixtures are inherited from the parent context, and may be replaced or modified by children.

[start-insert]: <../core/src/test/kotlin/dev/minutest/examples/fixtures/ParentFixtureExampleTests.kt>
```kotlin
class ParentFixtureExampleTests : JUnit5Minutests {

    data class Fixture(var fruit: String)

    fun tests() = rootContext<Fixture> {
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
<small>[../core/src/test/kotlin/dev/minutest/examples/fixtures/ParentFixtureExampleTests.kt](../core/src/test/kotlin/dev/minutest/examples/fixtures/ParentFixtureExampleTests.kt)</small>

[end-insert]: <>

## Changing Fixture Type

A context may change the type of its parent fixture.

[start-insert]: <../core/src/test/kotlin/dev/minutest/examples/DerivedContextExampleTests.kt>
```kotlin
// You can change the fixture type as you go down the context tree.
@Suppress("USELESS_IS_CHECK")
class DerivedContextExampleTests : JUnit5Minutests {

    // Fruit and FruitDrink are our 2 fixture types

    data class Fruit(val name: String)

    data class FruitDrink(val fruit: Fruit, val name: String) {
        override fun toString() = "${fruit.name} $name"
    }

    // Our root fixture type is Fruit
    fun tests() = rootContext<Fruit>("Fruit Context") {

        fixture {
            Fruit("banana")
        }

        test("takes Fruit") {
            assertTrue(fixture is Fruit)
        }

        // To change fixture type use derivedContext
        derivedContext<FruitDrink>("FruitDrink Context") {

            // deriveFixture specifies how to convert a Fruit to a FruitDrink
            deriveFixture {
                FruitDrink(parentFixture, "smoothie")
            }

            test("takes FruitDrink") {
                assertTrue(fixture is FruitDrink)
            }

            // If you don't need access to the parent fixture, this would do
            // fixture {
            //     FruitDrink(Fruit("kumquat"), "milkshake")
            // }
        }
    }
}
```
<small>[../core/src/test/kotlin/dev/minutest/examples/DerivedContextExampleTests.kt](../core/src/test/kotlin/dev/minutest/examples/DerivedContextExampleTests.kt)</small>

[end-insert]: <>

## Fixture Lifecyle

If a fixture has resources that should be disposed of, you can make it `AutoCloseable` and use `closeableFixture`.

[start-insert]: <../core/src/test/kotlin/dev/minutest/examples/CloseableFixtureExampleTests.kt>
```kotlin
class CloseableFixtureExampleTests : JUnit5Minutests {

    class Fixture(file: File): Closeable {

        val writer = FileWriter(file)

        override fun close() = writer.close()
    }

    fun tests() = rootContext<Fixture> {

        closeableFixture { testDescriptor ->
            Fixture(File.createTempFile(testDescriptor.name, ".tmp"))
        }

        test("can write") {
            writer.write("banana")
        }

        after {
            assertThrows(IOException::class.java) {
                writer.write("should be closed")
            }
        }
    }
}
```
<small>[../core/src/test/kotlin/dev/minutest/examples/CloseableFixtureExampleTests.kt](../core/src/test/kotlin/dev/minutest/examples/CloseableFixtureExampleTests.kt)</small>

[end-insert]: <>

## More Reading

[My New Test Model](http://oneeyedmen.com/my-new-test-model.html) discusses fixtures and contexts in more detail.

