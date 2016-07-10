#Millenial Media
-keepclassmembers class com.millennialmedia** {
public *;
}
-keep class com.millennialmedia**
-dontwarn com.millennialmedia**
#InMobi
-keep class com.inmobi.** { *; }
-dontwarn com.inmobi.**
#Crashlytics
-keep class com.crashlytics.** { *; }
-keep class com.crashlytics.android.**
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*
-keep public class * extends java.lang.Exception
#Solitaire
-keepattributes InnerClasses
-keep class com.tesseractmobile**
-dontwarn org.apache.http.**
-dontwarn android.net.http.AndroidHttpClient
-dontwarn com.google.android.gms.**
-dontwarn com.android.volley.toolbox.**
-dontwarn android.test.*
-keepattributes EnclosingMethod
-dontwarn com.squareup.javawriter.JavaWriter
-dontwarn sun.misc.Unsafe
-dontwarn org.hamcrest.**
-dontwarn junit.**
#Firebase
-keep class com.firebase.** { *; }
-keep class org.apache.** { *; }
-keepnames class com.fasterxml.jackson.** { *; }
-keepnames class javax.servlet.** { *; }
-keepnames class org.ietf.jgss.** { *; }
-dontwarn org.w3c.dom.**
-dontwarn org.joda.time.**
-dontwarn org.shaded.apache.**
-dontwarn org.ietf.jgss.**
#Catch all - remove when debugging
-dontwarn **