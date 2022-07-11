package github.xtvj.cleanx.utils

import android.content.Context
import android.os.Build
import coil.Coil
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.util.DebugLogger
import github.xtvj.cleanx.BuildConfig
import github.xtvj.cleanx.R

object CoilHolder {

    fun init(context: Context) {
        Coil.setImageLoader(ImageLoader.Builder(context)
            .components {
                // GIFs
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
                // SVGs
                add(SvgDecoder.Factory())
                // Video frames
//                add(VideoFrameDecoder.Factory())
            }
            .memoryCache {
                MemoryCache.Builder(context)
                    // Set the max size to 25% of the app's available memory.
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.filesDir.resolve("image_cache"))
                    .maxSizeBytes(512L * 1024 * 1024) // 512MB
                    .build()
            }
            .error(R.drawable.ic_broken_image) //错误图
//            .okHttpClient {
//                // Don't limit concurrent network requests by host.
//                val dispatcher = Dispatcher().apply { maxRequestsPerHost = maxRequests }
//
//                // Lazily create the OkHttpClient that is used for network operations.
//                OkHttpClient.Builder()
//                    .dispatcher(dispatcher)
//                    .build()
//            }
            // Show a short crossfade when loading images asynchronously.
            .crossfade(false)
            // Ignore the network cache headers and always read from/write to the disk cache.
            .respectCacheHeaders(false)
            // Enable logging to the standard Android log if this is a debug build.
            .apply { if (BuildConfig.DEBUG) logger(DebugLogger()) }
            .build())
    }

}