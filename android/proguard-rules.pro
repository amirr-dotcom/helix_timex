# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/bill/Library/Android/sdk/tools/proguard/proguard-android.txt
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


-keep class com.crrepa.ble.dfu.** { *; }
-dontwarn android.support.**
-dontwarn com.github.**
-dontwarn com.squareup.picasso.**
-dontwarn com.etsy.android.grid.**
-dontwarn android.support.v7.**

# Retrofit
-dontwarn retrofit2.**
-dontwarn org.codehaus.mojo.**
-keep class retrofit2.* { *; }
-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*

-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes RuntimeInvisibleParameterAnnotations

-keepattributes EnclosingMethod


-keepattributes InnerClasses
-keep public class com.quarantinepal.android**


-keepclasseswithmembers class * {
    @retrofit2.* <methods>;
}

-keepclasseswithmembers interface * {
    @retrofit2.* <methods>;
}
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

##--- Begin:GSON ----
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature
# POJOs used with GSON
# The variable names are JSON key values and should not be obfuscated
-keepclassmembers class com.quarantinepal.android** { <fields>; }
# You can apply the rule to all the affected classes also
# -keepclassmembers class com.example.apps.android.model.** { <fields>; }

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-keep class Sun.* { *; }
#-keep class com.google.gson.stream.** { *; }

# Prevent proguard from stripping interface information from TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# keep enum so gson can deserialize it
-keepclassmembers enum * { *; }


