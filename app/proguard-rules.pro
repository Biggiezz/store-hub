# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Preserve line numbers for crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# --- Gson: keep serialized model fields ---
-keepattributes Signature
-keepattributes *Annotation*
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# --- Retrofit: keep HTTP method annotations ---
-keepclasseswithmembers class * {
  @retrofit2.http.* <methods>;
}

# --- App model classes used by Gson/Retrofit (reflection-based) ---
-keep class com.nguyenmanhphuc.storehubapp.model.** { *; }

# --- ZaloPay SDK (no consumer rules) ---
-keep class vn.zalopay.** { *; }
-dontwarn vn.zalopay.**