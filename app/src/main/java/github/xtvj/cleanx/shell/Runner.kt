// SPDX-License-Identifier: GPL-3.0-or-later
package github.xtvj.cleanx.shell

import android.text.TextUtils
import androidx.annotation.StringDef
import androidx.annotation.WorkerThread
import github.xtvj.cleanx.utils.log
import java.io.InputStream
import java.util.*

abstract class Runner protected constructor() {
    @StringDef(MODE_AUTO, MODE_ROOT, MODE_ADB_OVER_TCP, MODE_ADB_WIFI, MODE_NO_ROOT)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation class Mode
    class Result(private val outputAsList: List<String?>, stderr: List<String?>, private val exitCode: Int) {

        @JvmOverloads
        constructor(exitCode: Int = 1) : this(emptyList<String>(), emptyList<String>(), exitCode) {
        }

        val isSuccessful: Boolean
            get() = exitCode == 0

        fun getOutputAsList(firstIndex: Int): List<String?> {
            return if (firstIndex >= outputAsList.size) {
                emptyList<String>()
            } else outputAsList.subList(firstIndex, outputAsList.size)
        }

        val output: String
            get() = TextUtils.join("\n", outputAsList)

        init {
            // Print stderr
            if (stderr.isNotEmpty()) log("Runner", TextUtils.join("\n", stderr))
        }
    }

    @JvmField
    protected val commands: MutableList<String>
    @JvmField
    protected val inputStreams: MutableList<InputStream>
    fun addCommand(command: String) {
        commands.add(command)
    }

    fun add(inputStream: InputStream) {
        inputStreams.add(inputStream)
    }

    fun clear() {
        commands.clear()
        inputStreams.clear()
    }

    @WorkerThread
    abstract fun runCommand(): Result
    private fun run(command: String, inputStream: InputStream?): Result {
        clear()
        addCommand(command)
        inputStream?.let { add(it) }
        return runCommand()
    }

    companion object {
        const val TAG = "Runner"
        const val MODE_AUTO = "auto"
        const val MODE_ROOT = "root"
        const val MODE_ADB_OVER_TCP = "adb_tcp"
        const val MODE_ADB_WIFI = "adb_wifi"
        const val MODE_NO_ROOT = "no-root"
        private var rootShellRunner: RootShellRunner? = null
        val instance: Runner
            get() = rootInstance
        @JvmStatic
        val rootInstance: Runner
            get() {
                if (rootShellRunner == null) {
                    rootShellRunner = RootShellRunner()
                    log("RootShellRunner")
                }
                return rootShellRunner!!
            }

        @Synchronized
        fun runCommand(command: String): Result {
            return runCommand(instance, command, null)
        }

        @Synchronized
        fun runCommand(command: Array<String?>): Result {
            return runCommand(instance, command, null)
        }

        @Synchronized
        fun runCommand(command: String, inputStream: InputStream?): Result {
            return runCommand(instance, command, inputStream)
        }

        @Synchronized
        fun runCommand(command: Array<String?>, inputStream: InputStream?): Result {
            return runCommand(instance, command, inputStream)
        }

        @Synchronized
        fun runCommand(runner: Runner, command: String): Result {
            return runner.run(command, null)
        }

        @Synchronized
        fun runCommand(runner: Runner, command: Array<String?>): Result {
            val cmd = StringBuilder()
            for (part in command) {
                cmd.append(RunnerUtils.escape(part)).append(" ")
            }
            return runCommand(runner, cmd.toString(), null)
        }

        @Synchronized
        fun runCommand(runner: Runner, command: String, inputStream: InputStream?): Result {
            return runner.run(command, inputStream)
        }

        @Synchronized
        fun runCommand(runner: Runner, command: Array<String?>, inputStream: InputStream?): Result {
            val cmd = StringBuilder()
            for (part in command) {
                cmd.append(RunnerUtils.escape(part)).append(" ")
            }
            return runCommand(runner, cmd.toString(), inputStream)
        }
    }

    init {
        commands = ArrayList()
        inputStreams = ArrayList()
    }
}