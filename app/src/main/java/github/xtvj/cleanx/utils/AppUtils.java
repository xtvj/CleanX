package github.xtvj.cleanx.utils;


import static github.xtvj.cleanx.utils.LogExtKt.log;

public class AppUtils {

    private AppUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    public static Boolean isAppRoot() {
        ShellUtils.CommandResult result = ShellUtils.execCmd("echo root", true);
        log(result.toString());
        return result.result == 0;
    }
}
