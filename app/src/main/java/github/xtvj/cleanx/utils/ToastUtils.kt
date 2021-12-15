package github.xtvj.cleanx.utils

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToastUtils @Inject constructor(val context: Context) {

    fun Context.toast(@StringRes res: Int) = Toast.makeText(this, res, Toast.LENGTH_SHORT).show()
    fun Context.toastLong(@StringRes res: Int) = Toast.makeText(this, res, Toast.LENGTH_LONG).show()

    fun Context.toast(message: CharSequence?) =
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    fun Context.toastLong(message: CharSequence?) =
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()

    fun toast(@StringRes res: Int) {
        Toast.makeText(context, res, Toast.LENGTH_SHORT).show()
    }

    fun toastLong(@StringRes res: Int) {
        Toast.makeText(context, res, Toast.LENGTH_LONG).show()
    }

    fun toast(message: CharSequence?) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun toastLong(message: CharSequence?) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }


}
