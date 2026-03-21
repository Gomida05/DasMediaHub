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
-renamesourcefileattribute SourceFile


# Keep Firebase services
-keep class com.google.firebase.Firebase.** { *; }

# Keep your Python code if you're using Chaquopy
-keep class com.chaquo.python.** { *; }

# === Google Identity (One Tap / GoogleID) ===

# Public API
-keep class com.google.android.gms.auth.api.identity.** { *; }

# Internal Identity implementation (package-private, required)
-keep class com.google.android.gms.auth.api.identity.internal.** { *; }

# Required synthetic + builder classes used by R8
-keep class com.google.android.gms.auth.api.identity.zz* { *; }

# Keep constructors only (prevents constructor mismatch)
-keepclassmembers class com.google.android.gms.auth.api.identity.** {
    <init>(...);
}

# Silence warnings without keeping everything
-dontwarn com.google.android.gms.auth.api.identity.**