# Release Notes

I still have to go through Slack posts to find older details, but in the meantime


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