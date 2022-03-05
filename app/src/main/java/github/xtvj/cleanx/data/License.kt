package github.xtvj.cleanx.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class License(
    val about: String,
    val address: String,
    val author: String,
    val license: String,
    val name: String,
    val version: String
)