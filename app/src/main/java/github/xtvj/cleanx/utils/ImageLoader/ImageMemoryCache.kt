// SPDX-License-Identifier: GPL-3.0-or-later
package github.xtvj.cleanx.utils.ImageLoader

import android.graphics.drawable.Drawable
import java.lang.ref.SoftReference
import java.util.*

internal class ImageMemoryCache {
    private val cache = Collections.synchronizedMap(HashMap<String, SoftReference<Drawable>>())
    operator fun get(id: String): Drawable? {
        val ref = cache[id] ?: return null
        return ref.get()
    }

    fun put(id: String, image: Drawable) {
        cache[id] = SoftReference(image)
    }

    fun clear() {
        cache.clear()
    }
}