@file:JvmName("MinutestRunnersThing")
package samples.runners

import uk.org.minutest.rootContext

fun tests() = rootContext<Unit> {
    runnersExample()
}
