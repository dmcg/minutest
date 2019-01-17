@file:JvmName("MinutestRunnersThing")
package samples.runners

import com.oneeyedmen.minutest.rootContext

fun tests() = rootContext<Unit> {
    runnersExample()
}
