package dev.minutest.junit

import dev.minutest.assertLogged
import samples.runners.expectedRunnersLog

fun checkRunnersExampleLog(
    log: List<String>,
    substitutions: List<Pair<String, String>>,
    hasExtraRoot: Boolean = false,
    abortRatherThanSkip: Boolean = false,
    expected: List<String>
) =
    assertLogged(
        log,
        *expectedRunnersLog(
            substitutions, hasExtraRoot, abortRatherThanSkip, expected
        ).toTypedArray()
    )