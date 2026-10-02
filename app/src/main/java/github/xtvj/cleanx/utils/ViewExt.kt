package github.xtvj.cleanx.utils

import android.widget.ImageView
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.databinding.BindingAdapter
import coil.load


@BindingAdapter(value = ["imageUri"], requireAll = true)
fun ImageView.loadImage(
    resId: Any?
) {
    resId?.let { load(it) }
}

fun View.applySystemBarPadding() {
    val initialLeft = paddingLeft
    val initialTop = paddingTop
    val initialRight = paddingRight
    val initialBottom = paddingBottom
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val insets = windowInsets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
        )
        view.updatePadding(
            left = initialLeft + insets.left,
            top = initialTop + insets.top,
            right = initialRight + insets.right,
            bottom = initialBottom + insets.bottom
        )
        windowInsets
    }
    ViewCompat.requestApplyInsets(this)
}
