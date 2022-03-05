# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# moshi
#-dontwarn javax.annotation.**
#
#-keepclasseswithmembers class * {
#    @com.squareup.moshi.* <methods>;
#}
#-keep @com.squareup.moshi.JsonQualifier @interface *
#-keepclassmembers @com.squareup.moshi.JsonClass class * extends java.lang.Enum {
#    <fields>;
#    **[] values();
#}
#-keepclassmembers class com.squareup.moshi.internal.Util {
#    private static java.lang.String getKotlinMetadataClassName();
#}
#-keepclassmembers class * {
#  @com.squareup.moshi.FromJson <methods>;
#  @com.squareup.moshi.ToJson <methods>;
#}