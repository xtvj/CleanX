package github.xtvj.cleanx.utils;


import android.util.Log;

public final class AppUtils {

    private AppUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    public static Boolean isAppRoot() {
        ShellUtils.CommandResult result = ShellUtils.execCmd("echo root", true);
        Log.d("AppUtils", result.toString());
        return result.result == 0;
    }
}
