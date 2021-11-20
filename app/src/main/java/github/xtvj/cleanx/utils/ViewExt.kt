package github.xtvj.cleanx.utils

import android.app.Service
import android.graphics.drawable.Drawable
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.annotation.DrawableRes
import coil.load
import coil.transform.CircleCropTransformation
import github.xtvj.cleanx.R

fun View.showKeyboard() {
    (this.context.getSystemService(Service.INPUT_METHOD_SERVICE) as? InputMethodManager)
            ?.showSoftInput(this, 0)
}

fun View.hideKeyboard() {
    (this.context.getSystemService(Service.INPUT_METHOD_SERVICE) as? InputMethodManager)
            ?.hideSoftInputFromWindow(this.windowToken, 0)
}

fun View.toVisible() {
    this.visibility = View.VISIBLE
}

fun View.toGone() {
    this.visibility = View.GONE
}

fun View.toInvisible() {
    this.visibility = View.GONE
}

fun ImageView.loadImage(@DrawableRes resId: Int) = load(resId)
fun ImageView.loadImage(url: String) = load(url){
    crossfade(true)
    placeholder(R.drawable.ic_launcher)
    transformations(CircleCropTransformation())
}

fun ImageView.loadImage(drawable: Drawable) = load(drawable)
