# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/and/Android/Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

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
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}
-dontobfuscate
-dontwarn org.hamcrest.**
-dontwarn com.squareup.**
-dontwarn com.google.android.**

-keep class com.google.android.** {
   *;
}
-keep class com.google.common.** {
   *;
}
-keep class org.hamcrest.** {
   *;
}
#For design support library
-keep class android.support.design.widget.** { *; }
-keep interface android.support.design.widget.** { *; }