package github.xtvj.cleanx.utils

import android.net.Uri
import android.widget.ImageView
import androidx.annotation.DrawableRes
import coil.load

fun ImageView.loadImage(@DrawableRes resId: Int) = load(resId)
fun ImageView.loadImage(resId: String) = load(resId)
fun ImageView.loadImage(resId: Uri) = load(resId)