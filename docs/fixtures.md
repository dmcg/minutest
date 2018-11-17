# [Minutest](README.md)

## Fixtures

The [JUnit 4 Wiki](https://github.com/junit-team/junit4/wiki/test-fixtures) says `A test fixture is a fixed state of a set of objects used as a baseline for running tests. The purpose of a test fixture is to ensure that there is a well known and fixed environment in which tests are run so that results are repeatable.`

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
    val keySwitch1: () -> Boolean,
    val keySwitch2: () -> Boolean,
    val beep: () -> Unit,
    val launchMissile: () -> Unit
) {
    fun pressButton() {
        if (keySwitch1() && keySwitch2())
            launchMissile()
        else
            beep()
    }
    val warningLight get() = keySwitch1() && keySwitch2()
}

class CompoundFixtureExampleTests : JupiterTests {

    class Fixture() {
        // Rather than introduce a mocking framework, we can work with
        // functions and mutable state.
        var switch1On = false
        var switch2On = false
        var beeped = false
        var missileLaunched = false

        val controlPanel = ControlPanel(
            keySwitch1 = { switch1On },
            keySwitch2 = { switch2On },
            beep = { beeped = true },
            launchMissile = { missileLaunched = true }
        )
    }

    override val tests = context<Fixture> {
        fixture { Fixture() }

        context("no keys turned") {
            modifyFixture {
                switch1On = true
            }
            test("light off") {
                assertFalse(controlPanel.warningLight)
            }
            test("cannot launch") {
                controlPanel.pressButton()
                assertTrue(beeped)
                assertFalse(missileLaunched)
            }
        }

        context("only key 1 turned") {
            modifyFixture {
                switch1On = true
            }
            test("light off") {
                assertFalse(controlPanel.warningLight)
            }
            test("cannot launch") {
                controlPanel.pressButton()
                assertTrue(beeped)
                assertFalse(missileLaunched)
            }
        }

        context("only key 2 turned") {
            // ...
        }

        context("both keys turned") {
            modifyFixture {
                switch1On = true
                switch2On = true
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
