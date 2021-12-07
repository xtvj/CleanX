package github.xtvj.cleanx.utils

import android.os.Build
import androidx.appcompat.app.AppCompatDelegate

object ThemeHelper {
    private const val LIGHT_MODE = "light"
    private const val DARK_MODE = "dark"
    private const val DEFAULT_MODE = "default"
    fun applyTheme(themePref: String) {
        log("theme: $themePref")
        when (themePref) {
            LIGHT_MODE -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            DARK_MODE -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            else -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
                }
            }
        }
    }
}