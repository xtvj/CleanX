package github.xtvj.cleanx.utils

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

object DateUtil {
    fun format(time: Long): String {
        val date = Date(time)
        @SuppressLint("SimpleDateFormat") val simpleDateFormat =
            SimpleDateFormat("yyyy.MM.dd")
        return simpleDateFormat.format(date)
    }
}