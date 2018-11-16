# [Minutest](README.md)

## Fixtures

The [JUnit 4 Wiki](https://github.com/junit-team/junit4/wiki/test-fixtures) says `A test fixture is a fixed state of a set of objects used as a baseline for running tests. The purpose of a test fixture is to ensure that there is a well known and fixed environment in which tests are run so that results are repeatable.`

Here are some ways of using fixtures in Minutest.

### No Fixture

You don't have to have a fixture. The simplest tests can just have assertions.

```insert-kotlin core/src/test/kotlin/com/oneeyedmen/Minutest/examples/NoFixtureExampleTests.kt
```

### Subject Under Test as Fixture

It makes sense to have the subject under test as the fixture if it has state and there is no additional state.

```insert-kotlin core/src/test/kotlin/com/oneeyedmen/Minutest/examples/SubjectUnderTestFixtureExampleTests.kt
```
 
### Arguments as Fixture

If you are testing static functions, making the arguments the fixture can be expressive. 

```insert-kotlin core/src/test/kotlin/com/oneeyedmen/Minutest/examples/ArgumentsAsFixtureExampleTests.kt
```

Again, where possible having the context name expressed in the fixture state, and vice-versa, keeps things honest.

More TBA