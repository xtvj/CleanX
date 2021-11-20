// SPDX-License-Identifier: GPL-3.0-or-later
package github.xtvj.cleanx.utils.ImageLoader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import github.xtvj.cleanx.utils.FileUtils
import github.xtvj.cleanx.utils.log
import java.io.*
import javax.inject.Inject

class FileCache @Inject constructor(val context: Context) {
    private var cacheDir: File? = null
    @Throws(IOException::class)
    fun putImage(name: String, drawable: Drawable?) {
        val iconFile = getImageFile(name)
        FileOutputStream(iconFile).use { os ->
            val bitmap = FileUtils.getBitmapFromDrawable(drawable!!)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
            os.flush()
        }
    }

    @Throws(FileNotFoundException::class)
    fun getImage(name: String): Drawable {
        val iconFile = getImageFile(name)
        return if (iconFile.exists() && iconFile.lastModified() >= lastModifiedDate) {
            Drawable.createFromStream(FileInputStream(iconFile), name)
        } else {
            throw FileNotFoundException("Icon for $name doesn't exist.")
        }
    }

    private fun getImageFile(name: String): File {
        return File(cacheDir, "$name.png")
    }

    fun clear() {
        val files = cacheDir!!.listFiles() ?: return
        var count = 0
        for (f in files) {
            if (f.lastModified() < lastModifiedDate) {
                if (f.delete()) ++count
            }
        }
        log("Cache", "Deleted $count images.")
    }

    companion object {
        private val lastModifiedDate = System.currentTimeMillis() - 604800000
    }

    init {
        cacheDir = if (context.externalCacheDir != null) {
            File(context.externalCacheDir, "images")
        } else {
            File(context.cacheDir, "images")
        }
        if (!cacheDir!!.exists()) cacheDir!!.mkdirs()
    }
}