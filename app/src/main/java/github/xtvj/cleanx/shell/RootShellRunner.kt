// SPDX-License-Identifier: GPL-3.0-or-later
package github.xtvj.cleanx.shell

import androidx.annotation.WorkerThread
import com.topjohnwu.superuser.Shell

internal class RootShellRunner : Runner() {
    @WorkerThread
    @Synchronized
    override fun runCommand(): Result {
        val shell: Shell.Job = Shell.su(*commands.toTypedArray())
        for (input in inputStreams) {
            shell.add(input)
        }
        val result = shell.exec()
        return Result(result.out, result.err, result.code)
    }

}