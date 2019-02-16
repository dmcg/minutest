# [Minutest](README.md)

## Cookbook

### How do I

#### Pronounce Minutest

The name originally came from a trawl of [words containing test](https://www.thefreedictionary.com/words-containing-test) - and *should* be pronounced as if it has the sense 'smallest'. 

However, when @DuncanMcG read back his first message on the subject `I'd like to run my new test framework past you`, he realised that there was another pronunciation - 'MyNewTest'.

That is the emphasis that seems to have stuck, at least with Duncan, but of course it is true for him. We're interested to see if it ever becomes talked about enough in real life to be an issue.  

#### Write tests

See the [readme](README.md)

#### Run Tests with JUnit 5

See the [readme](README.md)

#### Run Tests with JUnit 4

There is experimental support for running tests with JUnit 4 - see 
[JUnit4MinutestsTests](../core/src/test/kotlin/dev/minutest/junit/experimental/JUnit4MinutestsTests.kt)

#### Run Only Some Tests

There is experimental support for [skipping some tests and only running others](focus-and-skip.md)

#### Repeat a Test

[Repeating Tests](../core/src/test/kotlin/dev/minutest/examples/RepeatingExampleTests.kt)

#### Run a Test With Different Values

[Parameterised Tests](../core/src/test/kotlin/dev/minutest/examples/ParameterisedExampleTests.kt)

#### Generate tests on the fly

[Generating tests](generating-tests.md)

#### Change the Fixture Type Between Contexts

[Derived Contexts](../core/src/test/kotlin/dev/minutest/examples/DerivedContextExampleTests.kt)

#### Use JUnit Rules

JUnit Rules [mostly work](junit-rules.md).

#### Stop IntelliJ Showing Test Methods As Unused

Annotate the method with `dev.minutest.Tests`, Alt-Enter on the greyed-out method and select `Suppress unused warning if annotated by 'dev.minutest.Tests'`. Now all `@Tests` methods should not be greyed out. 

#### Get Help

The best bet for feedback and help is the [#minutest channel on the Kotlin Slack](https://kotlinlang.slack.com/messages/CCYE00YM6). See you there.

#### Reuse Test Code

TODO - in the meantime [just ask](https://kotlinlang.slack.com/messages/CCYE00YM6).


#### Manage Test Resources

TODO - in the meantime [just ask](https://kotlinlang.slack.com/messages/CCYE00YM6).

#### Trace Test Execution

TODO - in the meantime [just ask](https://kotlinlang.slack.com/messages/CCYE00YM6).

#### Mock Collaborators

TODO - in the meantime [just ask](https://kotlinlang.slack.com/messages/CCYE00YM6).

#### Think about Fixtures and Contexts

TODO - in the meantime [just ask](https://kotlinlang.slack.com/messages/CCYE00YM6).

#### Check a Value

TODO - in the meantime [just ask](https://kotlinlang.slack.com/messages/CCYE00YM6).

#### Structure Tests

TODO - in the meantime [just ask](https://kotlinlang.slack.com/messages/CCYE00YM6).

#### Extend Minutest

TODO - in the meantime [just ask](https://kotlinlang.slack.com/messages/CCYE00YM6).

#### Migrate from JUnit

TODO - in the meantime [just ask](https://kotlinlang.slack.com/messages/CCYE00YM6).