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

```insert-kotlin core/src/test/kotlin/dev/minutest/examples/NoFixtureExampleTests.kt
```

## Subject Under Test as Fixture

It makes sense to have the subject under test as the fixture if it has the only state in the test.

```insert-kotlin core/src/test/kotlin/dev/minutest/examples/SubjectUnderTestFixtureExampleTests.kt
```

Even if the subject is immutable then you can [inspect it in after blocks](immutable-fixtures.md).
 
## Arguments as Fixture

If you are testing static functions, making the arguments the fixture can be expressive. 

```insert-kotlin core/src/test/kotlin/dev/minutest/examples/ArgumentsAsFixtureExampleTests.kt
```

Again, where possible having the context name expressed in the fixture state, and vice-versa, keeps things honest.

## Compound Fixture

When testing a system that mediates between other components, it makes sense to bring them all into the fixture - this gives the test isolation and repeatability that is the point of the fixture.

```insert-kotlin core/src/test/kotlin/dev/minutest/examples/CompoundFixtureExampleTests.kt
```

## Parent Fixtures

Fixtures are inherited from the parent context, and may be replaced or modified by children.

```insert-kotlin core/src/test/kotlin/dev/minutest/examples/ParentFixtureExampleTests.kt
```

## Changing Fixture Type

A context may change the type of its parent fixture.

```insert-kotlin core/src/test/kotlin/dev/minutest/examples/DerivedContextExampleTests.kt
```

## More Reading

[My New Test Model](http://oneeyedmen.com/my-new-test-model.html) discusses fixtures and contexts in more detail.

