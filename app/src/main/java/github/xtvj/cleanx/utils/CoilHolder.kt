package github.xtvj.cleanx.utils

import android.app.Application
import androidx.core.app.ActivityCompat
import coil.Coil
import coil.ImageLoader
import github.xtvj.cleanx.R

object CoilHolder {

    fun init(application: Application) {
        Coil.setImageLoader(
            ImageLoader.Builder(application)
//                .placeholder(ActivityCompat.getDrawable(application, R.drawable.ic_default_round)) //占位符
                .error(ActivityCompat.getDrawable(application, R.drawable.ic_broken_image)) //错误图
                    //官方默认开启
//                .memoryCachePolicy(CachePolicy.ENABLED) //开启内存缓存
//                .diskCachePolicy(CachePolicy.ENABLED) //开启磁盘缓存
                .build()
        )
    }

}