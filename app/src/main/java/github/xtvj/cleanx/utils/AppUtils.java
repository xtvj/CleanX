package github.xtvj.cleanx.utils;



public final class AppUtils {

    private AppUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    public static boolean isAppRoot() {
        ShellUtils.CommandResult result = ShellUtils.execCmd("echo root", true);
        return result.result == 0;
    }
}
