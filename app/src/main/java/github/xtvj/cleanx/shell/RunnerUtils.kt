// SPDX-License-Identifier: GPL-3.0-or-later
package github.xtvj.cleanx.shell

import java.io.File

object RunnerUtils {
    fun escape(input: String): String = "'${input.replace("'", "'\\''")}'"

    fun isRootGiven(): Boolean {
        if (!isRootAvailable()) return false
        return Runner.runCommand(Runner.rootInstance(), "echo CleanXRoot")
            .output.contains("CleanXRoot")
    }

    private fun isRootAvailable(): Boolean =
        System.getenv("PATH")?.split(File.pathSeparatorChar)?.any { path ->
            File(path, "su").canExecute()
        } == true
}
