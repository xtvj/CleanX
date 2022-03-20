// SPDX-License-Identifier: GPL-3.0-or-later
package github.xtvj.cleanx.shell

import android.text.TextUtils
import androidx.annotation.WorkerThread
import github.xtvj.cleanx.utils.log
import java.io.InputStream

abstract class Runner protected constructor() {
    class Result(
        private val outputAsList: List<String>,
        stderr: List<String>,
        private val exitCode: Int
    ) {


        constructor(exitCode: Int = 1) : this(emptyList<String>(), emptyList<String>(), exitCode) {
        }

        val isSuccessful: Boolean
            get() = exitCode == 0

        fun getOutputAsList(firstIndex: Int): List<String> {
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


    protected val commands: MutableList<String>

    protected val inputStreams: MutableList<InputStream>
    private fun addCommand(command: String) {
        commands.add(command)
    }

    private fun add(inputStream: InputStream) {
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
        //todo 处理刚打开应用就需要root权限的问题
        private var rootShellRunner: RootShellRunner? = null
        private var userShellRunner: UserShellRunner? = null

        fun rootInstance(): Runner {
            if (rootShellRunner == null) {
                rootShellRunner = RootShellRunner()
            }
            log("RootShellRunner")
            return rootShellRunner!!
        }

        fun userInstance(): Runner {
            if (userShellRunner == null) {
                userShellRunner = UserShellRunner()
                log("userInstance")
            }
            return userShellRunner!!
        }

        @Synchronized
        fun runCommand(runner: Runner, command: String): Result {
            return runner.run(command, null)
        }

        @Synchronized
        fun runCommand(runner: Runner, command: Array<String>): Result {
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
        fun runCommand(runner: Runner, command: Array<String>, inputStream: InputStream?): Result {
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