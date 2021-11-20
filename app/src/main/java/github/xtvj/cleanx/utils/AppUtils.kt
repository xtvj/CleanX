package github.xtvj.cleanx.utils

class AppUtils private constructor() {
    companion object {
        val isAppRoot: Boolean
            get() {
                val result = ShellUtils.execCmd("echo root", true)
                log(result.toString())
                return result.result == 0
            }
    }

    init {
        throw UnsupportedOperationException("u can't instantiate me...")
    }
}