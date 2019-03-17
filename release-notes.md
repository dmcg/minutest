# Release Notes

I still have to go through Slack posts to find older details, but in the meantime

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