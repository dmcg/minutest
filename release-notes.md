# Release Notes

I still have to go through Slack posts to find older details, but in the meantime

## 2.0.0-rc3

Speed up test finding in the Minutest engine when running tests from a single class

## 2.0.0-rc2

### DSL changes (old methods deprecated)

* derivedContext -> context_

### Other

Experimental support for perturbation testing.

## 2.0.0-rc1

Publishing of v2 to Maven Central

## 2.0.0-alpha

### DSL changes (old methods deprecated)

* before -> beforeEach
* after -> afterEach
* fixture -> given
* modifyFixture -> beforeEach
* deriveFixture -> given_

### DSL changes (old methods renamed)

* test and test_ no longer take a TestDescriptor parameter to their block, but do supply the fixture as both receiver and parameter. The old versions are available as old_test and old_test_.

### Other DSL changes

DSL functions with TestDescriptors are available in an Instrumented object.

### Test Engine

The JUnit 5 (not Jupiter) test engine is now working well.
It should find methods or top-level functions annotated @Testable, and you can even point to methods in IntelliJ and run them.
Add this to Gradle to enable

```
tasks.withType<Test> {
    useJUnitPlatform {
        includeEngines(
            "junit-jupiter", 
            "minutest" <-- This
        )
    }
```

### Parallel Test Running

Run tests in each root context in parallel by setting a system property dev.minutest.parallel.
Parallel running isn't on by default yet, but seems solid.

## 1.13.0

As v.1.12.0 but published to Maven Central (if all goes well)

## 1.12.0

This release is preparing for a v2, with some DSL improvements, and 
parallel test running by default.

Run tests in each root context in parallel by setting a system property dev.minutest.parallel.

Lots of internal rework to support our own test engine and speeding things up.

Starting to decouple the DSL from the test building.

Some very minor API changes that I don't think anyone will notice. 

## 1.11.0

Experimental support for Scenarios

Internal changes

## 1.10.0

Jump to source in IntelliJ now works - double-click on a test to go to its definition. This works for contexts too, but
from the right-click menu.

## 1.9.0

Experimental support for Given When Then scenarios.


## 1.8.0

Internal changes to increase type safety, reduce object allocation and simplify.

## 1.7.0

A `rootContext {` function to avoid `rootContex<Unit> {`, thanks to Robert Stoll

Internal changes to reduce casting and increase type safety, thanks to Robert Stoll.

## 1.6.0

Internal changes to support safe transforms.

Running under Java 9, 10, 11, 12.


## 1.5.0

Annotation processing changes. These are not backward-compatible, but apply only to experimental features that I suspect
nobody but the developers are using. 

* Changes the precedence of multiple test annotations so that they apply in a human sort of way.

* Removes the `transform` parameter to the top level `rootContext` call in favour of using `RootTransforms` in a predicatable
way.

* Makes `RootTransform` a typealias for `NodeTransform<Unit>` and makes it a property of `TestAnnotation` rather than
a mixin

## 1.4.1

Fixes JUnit 4 Rules support to work properly in the case of derived fixtures changing the fixture type.

## 1.4.0

Add experimental support for flattening a Sequence of fixtures - the plan is to use this to support Property Based Tests.
See `FlatteningExampleTests`.  