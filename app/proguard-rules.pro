# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep JavascriptInterface annotation and methods annotated with it
-keepattributes JavascriptInterface
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Preserve lines for debugging production crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Fix for missing Error Prone annotations (often from Tink/androidx.security)
-dontwarn com.google.errorprone.annotations.**

