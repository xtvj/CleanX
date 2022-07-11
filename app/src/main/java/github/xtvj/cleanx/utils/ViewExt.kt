package github.xtvj.cleanx.utils

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import coil.load


@BindingAdapter(value = ["imageUri"], requireAll = true)
fun ImageView.loadImage(
    resId: Any?
) {
    load(resId)
}