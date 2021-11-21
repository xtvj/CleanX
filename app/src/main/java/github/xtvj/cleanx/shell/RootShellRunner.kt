// SPDX-License-Identifier: GPL-3.0-or-later
package github.xtvj.cleanx.shell

import androidx.annotation.WorkerThread
import com.topjohnwu.superuser.Shell

internal class RootShellRunner : Runner() {
    @WorkerThread
    @Synchronized
    override fun runCommand(): Result {
        val shell = Shell.su(*commands.toTypedArray())
        for (`is` in inputStreams) {
            shell.add(`is`)
        }
        val result = shell.exec()
        clear()
        return Result(result.out, result.err, result.code)
    }
}