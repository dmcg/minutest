package dev.minutest.junit

import dev.minutest.assertLogged
import samples.runners.expectedRunnersLog

fun checkRunnersExampleLog(
    log: List<String>,
    engineName: String,
    testName: String,
    rootName: String,
    abortRatherThanSkip: Boolean = false
) =
    assertLogged(
        log,
        *expectedRunnersLog(engineName, testName, rootName, abortRatherThanSkip).toTypedArray()
    )