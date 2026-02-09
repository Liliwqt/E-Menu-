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
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ==========================================
# Kotlinx Serialization
# ==========================================
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.example.androidkiosk.**$$serializer { *; }
-keepclassmembers class com.example.androidkiosk.** {
    *** Companion;
}
-keepclasseswithmembers class com.example.androidkiosk.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ==========================================
# Retrofit + OkHttp
# ==========================================
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

-dontwarn okhttp3.**
-dontwarn okio.**

# ==========================================
# Room
# ==========================================
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# ==========================================
# Firebase
# ==========================================
-keep class com.example.androidkiosk.model.MenuItem { *; }
-keep class com.example.androidkiosk.model.CategoryWithItems { *; }
-keep class com.example.androidkiosk.model.CartItem { *; }

# ==========================================
# Hilt
# ==========================================
-dontwarn dagger.hilt.**