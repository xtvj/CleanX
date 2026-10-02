// SPDX-License-Identifier: GPL-3.0-or-later
package github.xtvj.cleanx.shell

import androidx.annotation.WorkerThread
import com.topjohnwu.superuser.Shell

internal class RootShellRunner : Runner() {
    @WorkerThread
    @Synchronized
    override fun runCommand(): Result {
        val rootShell = Shell.getShell()
        if (!rootShell.isRoot) {
            return Result(emptyList(), listOf("Root access is unavailable"), 1)
        }
        val shell: Shell.Job = rootShell.newJob().add(*commands.toTypedArray())
        for (input in inputStreams) {
            shell.add(input)
        }
        val result = shell.exec()
        return Result(result.out, result.err, result.code)
    }

}
