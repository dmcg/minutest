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

It makes sense to have the subject under test as the fixture if it has state and there is no additional state.

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
 
### Arguments as Fixture

If you are testing static functions, making the arguments the fixture can be expressive. 

```kotlin
class ArgumentsAsFixtureExampleTests : JupiterTests {

    data class Arguments(val a: Int, val b: Int)

    override val tests = context<Arguments> {

        context("positive positive") {
            fixture {
                Arguments(3, 1)
            }
            test("addition") {
                assertEquals(4, a + b)
            }
            test("subtraction") {
                assertEquals(2, a - b)
            }
        }

        context("positive negative") {
            fixture {
                Arguments(3, -1)
            }
            test("addition") {
                assertEquals(2, a + b)
            }
            test("subtraction") {
                assertEquals(4, a - b)
            }
        }
    }
}
```

Again, where possible having the context name expressed in the fixture state, and vice-versa, keeps things honest.

More TBA