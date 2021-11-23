// SPDX-License-Identifier: GPL-3.0-or-later
package github.xtvj.cleanx.shell

import androidx.annotation.WorkerThread
import com.topjohnwu.superuser.Shell

internal class UserShellRunner : Runner() {
    @WorkerThread
    @Synchronized
    override fun runCommand(): Result {
        val shell = Shell.sh(*commands.toTypedArray())
        for (input in inputStreams) {
            shell.add(input)
        }
        val result = shell.exec()
        clear()
        return Result(result.out, result.err, result.code)
    }
}