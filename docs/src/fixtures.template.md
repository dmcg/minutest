# [Minutest](README.md)

## Fixtures

The [JUnit 4 Wiki](https://github.com/junit-team/junit4/wiki/test-fixtures) says:

> A test fixture is a fixed state of a set of objects used as a baseline for running tests. The purpose of a test fixture is to ensure that there is a well known and fixed environment in which tests are run so that results are repeatable.

Here are some ways of using fixtures in Minutest.

### No Fixture

You don't have to have a fixture. The simplest tests can just have assertions.

```insert-kotlin core/src/test/kotlin/com/oneeyedmen/minutest/examples/NoFixtureExampleTests.kt
```

### Subject Under Test as Fixture

It makes sense to have the subject under test as the fixture if it has the only state in the test.

```insert-kotlin core/src/test/kotlin/com/oneeyedmen/minutest/examples/SubjectUnderTestFixtureExampleTests.kt
```

Even if the subject is immutable then you can [inspect it in after blocks](immutable-fixtures.md).
 
### Arguments as Fixture

If you are testing static functions, making the arguments the fixture can be expressive. 

```insert-kotlin core/src/test/kotlin/com/oneeyedmen/minutest/examples/ArgumentsAsFixtureExampleTests.kt
```

Again, where possible having the context name expressed in the fixture state, and vice-versa, keeps things honest.

### Compound Fixture

When testing a system that mediates between other components, it makes sense to bring them all into the fixture - this gives the test isolation and repeatability that is the point of the fixture.

```insert-kotlin core/src/test/kotlin/com/oneeyedmen/minutest/examples/CompoundFixtureExampleTests.kt
```

### Parent Fixtures

Fixtures are inherited from the parent context, and may be replaced or modified by children.

```insert-kotlin core/src/test/kotlin/com/oneeyedmen/minutest/examples/ParentFixtureExampleTests.kt
```

### Changing Fixture Type

A context may change the type of its parent fixture.

```insert-kotlin core/src/test/kotlin/com/oneeyedmen/minutest/examples/DerivedContextExampleTests.kt
```


