# Minutest

[ ![Download](https://api.bintray.com/packages/dmcg/oneeyedmen-mvn/minutest/images/download.svg) ](https://bintray.com/dmcg/oneeyedmen-mvn/minutest/_latestVersion)

Minutest brings Spec-style testing to JUnit 5 and Kotlin.

## Installation

I don't think that Minutest is ready for Android or KotlinJS or KotlinNative projects yet. Sorry. If you prove me wrong please let me know.

You can find the latest binaries and source in a Maven-compatible format on [JCenter](https://bintray.com/dmcg/oneeyedmen-mvn/minutest). So you need to reference that JCenter as a repository.

```groovy
repositories {
    jcenter()
}
```

You will need to include Minutest and JUnit 5 on your test compilation classpath, and the JUnit engine on your test runtime classpath. 

```groovy
testCompile "org.junit.jupiter:junit-jupiter-api:+"
testCompile "com.oneeyedmen:minutest:+"

testRuntime "org.junit.jupiter:junit-jupiter-engine:+"
testRuntime "org.junit.platform:junit-platform-launcher:+"
```

Finally you need to let test tasks know to use JUnit 5

```groovy
tasks {
    test {
        useJUnitPlatform()
        testLogging {
            events "skipped", "failed"
        }
    }
}
```

My apologies to the Mavenites. If you are one then please try to work out what to do based on the [JUnit 5 docs](https://junit.org/junit5/docs/current/user-guide/#installation) and then submit a PR for this readme!

## Usage

To just test simple functions, define your tests in a subclass of JUnitTests. The JUnit 5 [first test case](https://junit.org/junit5/docs/current/user-guide/#writing-tests) looks like this.

```kotlin
// Minutests are usually defined in a object.
// Extend JUnitTests to have them run by JUnit 5
object FirstMinutests : JUnitTests<Unit>({

    // define a test by calling test
    test("my first test") {
        // Minutest doesn't have any built-in assertions.
        // Here I'm using JUnit assertEquals
        assertEquals(2, 1 + 1)
    }
})
```

Most tests require access to some state. The collection of state required by the tests is called the test fixture. If you are testing a class, at simplest the fixture might be an instance of the class.

```kotlin
// The fixture type is the generic type of the test, here Stack<String>
object SimpleStackExampleTests : JUnitTests<Stack<String>>({

    // Instead of defining the fixture as a field of the test like JUnit,
    // in Minutest you call 'fixture' to initialise it for every test.
    fixture { Stack() }

    // In a test, 'this' is the fixture created above
    test("run first") {
        assertTrue(this.isEmpty())

        // you can leave out 'this'
        add("item")
        assertFalse(isEmpty())
    }

    // another test will use a new fixture instance
    test("run second") {
        assertTrue(this.isEmpty())
    }
})


```

More complicated tests will have more than one piece of state. 

```kotlin
// This time we're not going to extend JUnitTests.
// This lets us define the Fixture type inside the test object
object FixtureExampleTests {

    // We have multiple state, so make a separate fixture class
    class Fixture {
        // these would be the fields of your JUnit test
        val stack1 = Stack<String>()
        val stack2 = Stack<String>()
    }

    // Instead of extending JUnitTests, we declare a @TestFactory method.
    // JUnit will run the tests returned
    @TestFactory fun test() = junitTests<Fixture> {

        // Again the fixture is created once for each test
        fixture { Fixture() }

        // and access it in tests
        test("swap top") {
            stack1.push("on one")
            stack2.push("on two")
            stack1.swapTop(stack2)
            assertEquals("on two", stack1.peek())
            assertEquals("on one", stack2.peek())
        }
    }
}


private fun <E> Stack<E>.swapTop(otherStack: Stack<E>) {
    val myTop = pop()
    push(otherStack.pop())
    otherStack.push(myTop)
}
```

Minutests can be defined in a Spec style, with nested contexts and tests. The JUnit 5 [Nested Tests example](https://junit.org/junit5/docs/current/user-guide/#writing-tests-nested) translates like this 

```kotlin
object StackExampleTests {

    @TestFactory
    fun `when new`() = junitTests<Stack<String>> {

        // in this case the test fixture is just the stack we are testing
        fixture { Stack() }

        test("is empty") {
            assertTrue(this.isEmpty())
        }

        test("throws EmptyStackException when popped") {
            assertThrows<EmptyStackException> { pop() }
        }

        test("throws EmptyStackException when peeked") {
            assertThrows<EmptyStackException> { peek() }
        }

        // nested context
        context("after pushing an element") {

            // this context modifies the fixture from its parent
            modifyFixture { push("one") }

            test("is not empty") {
                assertFalse(isEmpty())
            }

            test("returns the element when popped and is empty") {
                assertEquals("one", pop())
                assertTrue(isEmpty())
            }

            test("returns the element when peeked but remains not empty") {
                assertEquals("one", peek())
                assertFalse(isEmpty())
            }
        }
    }
}

```

This runs the following tests

![StackExampleTests](docs/images/StackExampleTests.png)

## Parameterised Tests

The key to Minutest is that by separating the fixture from the test code, both are made available to manipulate as data. 

For example, parameterised tests require [special handling](https://junit.org/junit5/docs/current/user-guide/#writing-tests-parameterized-tests) in JUnit, but not in Minutest.

```kotlin
object ParameterisedTests {

    // Here we don't bother with a fixture, hence <Unit>
    @TestFactory fun palindromeTests() = junitTests<Unit> {

        // Running the same tests for multiple parameters is as easy as calling `test()` for each one.
        listOf("a", "oo", "racecar", "able was I ere I saw elba").forEach { candidate ->
            test("$candidate is a palindrome") {
                assertTrue(candidate.isPalindrome())
            }
        }

        listOf("", "ab", "a man a plan a canal pananma").forEach { candidate ->
            test("$candidate is not a palindrome") {
                assertFalse(candidate.isPalindrome())
            }
        }
    }
}

fun String.isPalindrome(): Boolean =
    if (length == 0) false
    else (0 until length / 2).find { index -> this[index] != this[length - index - 1] } == null
```

![ParameterisedTests](docs/images/ParameterisedTests.png)

## Reusing Tests

More complicated scenarios can be approached by writing your own function that returns a test or a context.
 
If you want to reuse the same tests for different concrete implementations, define a context with a function and call it for subclasses. Some people call this a contract.

```kotlin
// To run the same tests against different implementations, first define a function
// taking the implementation and returning a TestContext
fun TestContext<MutableCollection<String>>.behavesAsMutableCollection(
    collectionName: String,
    factory: () -> MutableCollection<String>
) {
    context("$collectionName behaves as MutableCollection") {

        fixture { factory() }

        test("is empty") {
            assertTrue(isEmpty())
        }

        test("can add") {
            add("item")
            assertEquals("item", first())
        }
    }
}

// Now tests can invoke the function to define a context to be run

object ArrayListTests {
    @TestFactory fun tests() = junitTests<MutableCollection<String>> {
        behavesAsMutableCollection("ArrayList") { ArrayList() }
    }
}

object LinkedListTests{
    @TestFactory fun tests() = junitTests<MutableCollection<String>> {
        behavesAsMutableCollection("LinkedList") { LinkedList() }
    }
}
```

## Generate Tests

Go crazy and unleash the `Power of Kotlin` to generate your tests on the fly.

```kotlin
// We can define functions that return tests for later injection

private typealias StringStack = Stack<String>

private fun TestContext<StringStack>.isEmpty(isEmpty: Boolean) =
    test("is " + (if (isEmpty) "" else "not ") + "empty") {
        assertEquals(isEmpty, size == 0)
        if (isEmpty)
            assertThrows<EmptyStackException> { peek() }
        else
            assertNotNull(peek())
    }

private fun TestContext<StringStack>.canPush() = test("can push") {
    val initialSize = size
    val item = "*".repeat(initialSize + 1)
    push(item)
    assertEquals(item, peek())
    assertEquals(initialSize + 1, size)
}

private fun TestContext<StringStack>.canPop() = test("can pop") {
    val initialSize = size
    val top = peek()
    assertEquals(top, pop())
    assertEquals(initialSize - 1, size)
    if (size > 0)
        assertNotEquals(top, peek())
}

private fun TestContext<StringStack>.cantPop() = test("cant pop") {
    assertThrows<EmptyStackException> { pop() }
}

object GeneratingExampleTests {

    @TestFactory fun `stack tests`() = junitTests<StringStack> {

        fixture { StringStack() }

        context("an empty stack") {
            // invoke the functions to create tests
            isEmpty(true)
            canPush()
            cantPop()
        }

        context("a stack with one item") {
            modifyFixture { push("one") }

            isEmpty(false)
            canPush()
            canPop()

            test("has the item on top") {
                assertEquals("one", peek())
            }
        }
    }

    @TestFactory fun `multiple tests on multiple stacks`() = junitTests<StringStack> {

        // here we generate a context with 3 tests for each of 4 stacks
        (0..3).forEach { itemCount ->
            context("stack with $itemCount items") {

                fixture {
                    StringStack().apply {
                        (1..itemCount).forEach { add(it.toString()) }
                    }
                }

                isEmpty(itemCount == 0)
                canPush()
                canPop(itemCount > 0)
            }
        }
    }
}

private fun TestContext<StringStack>.canPop(canPop: Boolean) = if (canPop) canPop() else cantPop()
```

The last of these generates the following tests

![MultipleStackExamples](docs/images/MultipleStackExamples.png)

## Immutable Fixtures

Are you a functional programmer slumming it with Kotlin? Minutest allows immutable fixtures.

```kotlin
object ImmutableExampleTests {

    // If you like this FP stuff, you may want to test an immutable fixture.

    @TestFactory fun `immutable fixture`() = junitTests<List<String>> {
        fixture { emptyList() }

        // test_ allows you to return the fixture
        test_("add an item and return the fixture") {
            val newList = this + "item"
            assertEquals("item", newList.first())
            newList
        }

        // which will be available for inspection in after
        after {
            println("in after")
            assertEquals("item", first())
        }

        // there are also before_ and after_ which return new fixtures
    }
}
```

## JUnit Rules

Power JUnit 4 user? Minutest supports JUnit 4 TestRules. As far as I can tell, it does it better than JUnit 5!

```kotlin
object JunitRulesExampleTests {

    class Fixture {
        // make rules part of the fixture, no need for an annotation
        val testFolder = TemporaryFolder()
    }

    @TestFactory fun `temporary folder rule`() = junitTests<Fixture>() {

        fixture { Fixture() }

        // tell the context to use the rule for each test in it and its children
        applyRule { this.testFolder }

        // and it will apply in this and sub-contexts
        test("test folder is present") {
            assertTrue(testFolder.newFile().isFile)
        }
    }
}
```