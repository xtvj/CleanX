package github.xtvj.cleanx.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object FileUtils {
    fun getApkShareUri(context: Context, packageName: String, sourcePath: String): Uri {
        val shareDirectory = File(context.cacheDir, "shared_apks").apply { mkdirs() }
        val sharedApk = File(shareDirectory, "$packageName.apk")
        File(sourcePath).inputStream().use { input ->
            sharedApk.outputStream().use { output -> input.copyTo(output) }
        }
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            sharedApk
        )
    }
}
