package github.xtvj.cleanx.utils

import android.content.Context
import android.content.res.Resources
import android.widget.Toast
import androidx.annotation.StringRes

    fun Context.toast(@StringRes res: Int) = Toast.makeText(this, res, Toast.LENGTH_SHORT).show()
    fun Context.toastLong(@StringRes res: Int) = Toast.makeText(this, res, Toast.LENGTH_LONG).show()

    fun Context.toast(message: CharSequence?) =
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    fun Context.toastLong(message: CharSequence?) =
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()


    fun Context.screenWidth() = Resources.getSystem().displayMetrics.widthPixels

    fun Context.screenHeight() = Resources.getSystem().displayMetrics.heightPixels
