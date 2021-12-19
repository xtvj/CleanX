package github.xtvj.cleanx.utils

import android.os.Build
import java.util.*

/**
 * Constants used throughout the app.
 */
const val DATABASE_NAME = "cleanX-db"


const val APPS_BY_ID = "id"
const val APPS_BY_NAME = "name"
const val APPS_BY_LAST_UPDATE_TIME = "lastUpdateTime"

val CMD_PM = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) "cmd package" else "pm"
val GET_All = "$CMD_PM list packages"
val GET_USER = "$CMD_PM list packages -3"
val GET_SYS = "$CMD_PM list packages -s"
val GET_DISABLED = "$CMD_PM list packages -d"

const val FORCE_STOP = "am force-stop "
val PM_DISABLE = "$CMD_PM disable-user "
val PM_ENABLE = "$CMD_PM enable "

var ALL_UUID: UUID? = null