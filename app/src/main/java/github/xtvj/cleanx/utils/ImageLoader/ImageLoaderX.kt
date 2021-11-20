// SPDX-License-Identifier: GPL-3.0-or-later
package github.xtvj.cleanx.utils.ImageLoader

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.topjohnwu.superuser.internal.UiThreadHandler
import java.io.FileNotFoundException
import java.io.IOException
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject

class ImageLoaderX @Inject constructor(val pm: PackageManager,val fileCache: FileCache): AutoCloseable {
    private val memoryCache = ImageMemoryCache()
    private val imageViews = Collections.synchronizedMap(WeakHashMap<ImageView, String>())
    private val executor: ExecutorService  = Executors.newFixedThreadPool(5)
//    private val shutdownExecutor: Boolean
    private var isClosed = false

//    constructor() {
//        executor = Executors.newFixedThreadPool(5)
//        shutdownExecutor = true
//    }
//
//    constructor(executor: ExecutorService) {
//        this.executor = executor
//        shutdownExecutor = false
//    }

    fun displayImage(name: String, imageView: ImageView) {
        imageViews[imageView] = name
        val image = memoryCache[name]
        if (image != null) imageView.setImageDrawable(image) else {
            queueImage(name, imageView)
        }
    }

    private fun queueImage(name: String, imageView: ImageView) {
        val queueItem = ImageLoaderQueueItem(pm,name, imageView)
        executor.submit(LoadQueueItem(queueItem))
    }

    override fun close() {
        isClosed = true
//        if (shutdownExecutor) {
//            executor.shutdownNow()
//        }
        memoryCache.clear()
        fileCache.clear()
    }


    internal class ImageLoaderQueueItem(
        val pm: PackageManager,
        val name: String,
        val imageView: ImageView
    ){

    }


    private inner class LoadQueueItem(var queueItem: ImageLoaderQueueItem) :
        Runnable {
        override fun run() {
            if (imageViewReusedOrClosed(queueItem)) return
            var image: Drawable? = null
            try {
                image = fileCache.getImage(queueItem.name)
            } catch (ignored: FileNotFoundException) {
            }
            if (image == null) { // Cache miss
                try {
                    val info = pm.getApplicationInfo(queueItem.name, 0)
                    image = info.loadIcon(queueItem.pm)
                    try {
                        fileCache.putImage(queueItem.name, image)
                    } catch (ignore: IOException) {
                    }
                } catch (ignored: PackageManager.NameNotFoundException) {
                }
            }
            memoryCache.put(queueItem.name, image!!)
            if (imageViewReusedOrClosed(queueItem)) return
            UiThreadHandler.run(LoadImageInImageView(image, queueItem))
        }
    }

    //Used to display bitmap in the UI thread
    private inner class LoadImageInImageView(
        private val image: Drawable,
        private val queueItem: ImageLoaderQueueItem
    ) : Runnable {
        override fun run() {
            if (imageViewReusedOrClosed(queueItem)) return
            queueItem.imageView.setImageDrawable(image)
        }
    }

    private fun imageViewReusedOrClosed(imageLoaderQueueItem: ImageLoaderQueueItem): Boolean {
        val tag = imageViews[imageLoaderQueueItem.imageView]
        return isClosed || tag == null || tag != imageLoaderQueueItem.name
    }
}