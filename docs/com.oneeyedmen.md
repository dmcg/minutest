[Minutest](README.md)

[Minutest](README.md)

[Minutest](README.md)

[Minutest](README.md)

[Minutest](README.md)

[Minutest](README.md)

[Minutest](README.md)

[Minutest](README.md)

[Minutest](README.md)

[Minutest](README.md)

[Minutest](README.md)

[Minutest](README.md)

[Minutest](README.md)

[Minutest](README.md)

[Minutest](README.md)

[Minutest](README.md)

[Minutest](README.md)

[Minutest](README.md)

[Minutest](README.md)

# com.oneeyedmen.minutest

Minutest was previously published as `com.oneeyedmen.minutest`. I have recently (2019-02-08) moved to its own domain `dev.minutest`, but in the process have lost the previous versions published on Bintray. 

Because of limitations with Bintray I cannot completely rectify this situation. Instead I have published version 0.43.0 (the last under com.oneeyedmen) with the Maven coordinates `dev.minutest:minutest:0.43.0` but the old `com.oneeyedmen.minutest` package.

If you are using Minutest prior to version 0.45.0 please update your build to reference `testImplementation "dev.minutest:minutest:0.43.0"` and that should get you back building. Then update to reference `testImplementation "dev.minutest:minutest:+"` and `s/com.oneeyedmen.minutest/dev.minutest/minutest/g` in your Kotlin source. 

I'm really sorry about the hassle - I screwed up. If you need help migrating, or want to how it happened and how to prevent it happening to you, please ask on the [#minutest channel on the Kotlin Slack](https://kotlinlang.slack.com/messages/CCYE00YM6).