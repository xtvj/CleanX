package github.xtvj.cleanx.data

import androidx.annotation.Keep

@Keep
data class License(
    val about: String,
    val address: String,
    val author: String,
    val license: String,
    val name: String,
    val version: String
)
