[Minutest](README.md)

# Cookbook

## Pronouncing Minutest

The name originally came from a trawl of [words containing test](https://www.thefreedictionary.com/words-containing-test) - and so *should* be pronounced as if it has the sense 'smallest'. 

However, when @duncanmcg read back his first message on the subject `I'd like to run my new test framework past you`, he realised that there was another pronunciation - 'My New Test', and that is the emphasis that seems to have stuck.
 
## Writing Tests

See the [readme](README.md)

## Running Tests with JUnit 5

See the [readme](README.md)

## Running Tests with JUnit 4

There is experimental support for running tests with JUnit 4 - see 
[JUnit4MinutestsTests](../core/src/test/kotlin/dev/minutest/junit/experimental/JUnit4MinutestsTests.kt)

## Understanding Fixtures and Contexts

Understanding fixtures is key to Minutest - [read more](fixtures.md)

## Running Only Some Tests

There is experimental support for [skipping some tests and only running others](focus-and-skip.md)

## Repeating Tests

[Repeating Tests](../core/src/test/kotlin/dev/minutest/examples/RepeatingExampleTests.kt)

## TDD with Minutest

[Test Driven to Specification with Minutest - Part 1](http://oneeyedmen.com/test-driven-to-specification-with-minutest-part1.html)

## Specs with Minutest

[Test Driven to Specification with Minutest - Part 2](http://oneeyedmen.com/test-driven-to-specification-with-minutest-part2.html)

## Running a Test With Different Values

[Parameterised Tests](../core/src/test/kotlin/dev/minutest/examples/ParameterisedExampleTests.kt)

## Generating tests on the fly

[Generating tests](generating-tests.md)

## Changing the Fixture Type Between Contexts

[Derived Contexts](../core/src/test/kotlin/dev/minutest/examples/DerivedContextExampleTests.kt)

## Using JUnit Rules

JUnit Rules [mostly work](junit-rules.md).

## Mocking Collaborators

[JMock](../core/src/test/kotlin/dev/minutest/examples/experimental/JMockExampleTests.kt)

## Approval Tests with Minutest

It's easy to integrate [Okeydoke](https://github.com/dmcg/okey-doke/) - see [Minutest Examples](https://github.com/dmcg/minutest-examples/blob/master/src/test/kotlin/approvals/ApprovalsTest2.kt)

## Stopping IntelliJ Showing Test Methods As Unused

Annotate the method with `dev.minutest.Tests`, Alt-Enter on the greyed-out method and select `Suppress unused warning if annotated by 'dev.minutest.Tests'`. Now all `@Tests` methods should not be greyed out. 

## Testing with Coroutines

[Simple tests with coroutines](../core/src/test/kotlin/dev/minutest/examples/experimental/CoroutinesExampleTests.kt)

[More elaborate tests with coroutines](../core/src/test/kotlin/dev/minutest/examples/experimental/CoroutinesExampleTests2.kt)

## Structuring Tests

[Given When Then](../core/src/test/kotlin/dev/minutest/examples/scenarios/ScenariosExampleTests.kt)

## Reusing Test Code

[Contracts](../core/src/test/kotlin/dev/minutest/examples/ContractsExampleTests.kt)

## Getting Help

The best bet for feedback and help is the [#minutest channel on the Kotlin Slack](https://kotlinlang.slack.com/messages/CCYE00YM6). See you there.



## Managing Test Resources

TODO - in the meantime [just ask](https://kotlinlang.slack.com/messages/CCYE00YM6).

## Tracing Test Execution

TODO - in the meantime [just ask](https://kotlinlang.slack.com/messages/CCYE00YM6).

## Checking a Value

TODO - in the meantime [just ask](https://kotlinlang.slack.com/messages/CCYE00YM6).

## Extending Minutest

TODO - in the meantime [just ask](https://kotlinlang.slack.com/messages/CCYE00YM6).

## Migrating from JUnit

TODO - in the meantime [just ask](https://kotlinlang.slack.com/messages/CCYE00YM6).