package github.xtvj.cleanx.utils

import android.net.Uri
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide

// coil 使用最新版1.4.0和开发版2.0.0-alpha05均出现图片闪烁的情况
//fun ImageView.loadImage(@DrawableRes resId: Int) = load(resId)
//fun ImageView.loadImage(resId: String) = load(resId)
//fun ImageView.loadImage(resId: Uri) = load(resId)

//Glide 稳定没毛病
//fun ImageView.loadImage(@DrawableRes resId: Int) = Glide.with(this).load(resId).into(this)
//fun ImageView.loadImage(resId: String) = Glide.with(this).load(resId).into(this)
//fun ImageView.loadImage(resId: Uri) = Glide.with(this).load(resId).into(this)
@BindingAdapter("imageUri")
fun ImageView.loadImage(resId: Any?): Any {
    return when (resId) {
        is Int -> {
            Glide.with(this).load(resId).into(this)
        }
        is Uri -> {
            Glide.with(this).load(resId).into(this)
        }
        is String -> {
            Glide.with(this).load(resId).into(this)
        }
        else -> {

        }
    }
}