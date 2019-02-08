@file:JvmName("MinutestRunnersThing")
package samples.runners

import dev.minutest.rootContext

fun tests() = rootContext<Unit> {
    runnersExample()
}
