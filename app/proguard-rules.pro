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

# We only want obfuscation
-keepattributes InnerClasses
-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keep class com.fluidtouch.noteshelf.** { *; }
-keep class com.fluidtouch.clipart.** { *; }
-keep class com.wang.avi.** { *; }

-keep class com.fluidtouch.dynamicgeneration.** { *; }

-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }
-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}

-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}
-keepclassmembers class com.codepath.models** { <fields>; }
-keep class sun.misc.Unsafe { *; }

-keep interface org.parceler.Parcel
-keep @org.parceler.Parcel class * { *; }
-keep class **$$Parcelable { *; }


-optimizations !code/simplification/arithmetic


-dontskipnonpubliclibraryclasses
-forceprocessing
-optimizationpasses 5
-overloadaggressively

# Removing logging code
-assumenosideeffects class android.util.Log {
public static *** d(...);
public static *** v(...);
public static *** i(...);
public static *** w(...);
public static *** e(...);
}

# The -dontwarn option tells ProGuard not to complain about some artefacts in the Scala runtime

# Appcompat and support
-keep interface android.support.v7.** { *; }
-keep class android.support.v7.** { *; }
-keep interface android.support.v4.** { *; }
-keep class android.support.v4.** { *; }
-dontwarn android.support.**
-dontwarn com.google.android.material.**
-dontwarn android.app.Notification
-dontwarn org.apache.log4j.**
-dontwarn com.google.common.**


-keepclassmembers class com.codepath.models** { <fields>; }
-keep class sun.misc.Unsafe { *; }

# Okio
-keep class sun.misc.Unsafe { *; }
-dontwarn java.nio.file.*
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn com.google.errorprone.annotations.*

#retrofit
-dontwarn retrofit.**
-keep class retrofit.** { *; }
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# zendesk Sdk
-keep public interface com.zendesk.** { *; }
-keep public class com.zendesk.** { *; }
-dontwarn java.awt.**

-keep class zendesk.core.AuthenticationRequestWrapper { *; }
-keep class zendesk.core.PushRegistrationRequestWrapper { *; }
-keep class zendesk.core.PushRegistrationRequest { *; }
-keep class zendesk.core.PushRegistrationResponse { *; }
-keep class zendesk.core.ApiAnonymousIdentity { *; }
-keep class zendesk.support.Comment { *; }
-keep class zendesk.support.CreateRequest { *; }
-keep class zendesk.support.CreateRequestWrapper { *; }
-keep class zendesk.support.EndUserComment { *; }
-keep class zendesk.support.Request { *; }
-keep class zendesk.support.UpdateRequestWrapper { *; }

# Gson
-keep interface com.google.gson.** { *; }
-keep class com.google.gson.** { *; }

#Picasso
-dontwarn com.squareup.okhttp.**
-dontwarn javax.annotation.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
-dontwarn org.codehaus.mojo.animal_sniffer.*
-dontwarn okhttp3.internal.platform.ConscryptPlatform

#ok http
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

#crashlytics
#-keep class com.crashlytics.** { *; }
#-keep class com.crashlytics.android.**
#-keep public class * extends java.lang.Exception
#-dontwarn com.crashlytics.**

#Firbase
# 3P providers are optional
-dontwarn com.facebook.**
# Keep the class names used to check for availablility
-keepnames class com.facebook.login.LoginManager
-keepnames class com.twitter.sdk.android.core.identity.TwitterAuthClient

# Don't note a bunch of dynamically referenced classes
-dontnote com.google.**
-dontnote com.facebook.**
-dontnote com.twitter.**
-dontnote com.squareup.okhttp.**
-dontnote okhttp3.internal.**

# Retrofit config
-dontnote retrofit2.Platform

# TODO remove https://github.com/google/gson/issues/1174
-dontwarn com.google.gson.Gson$6

### greenDAO 3
-keepclassmembers class * extends org.greenrobot.greendao.AbstractDao {
public static java.lang.String TABLENAME;
}
-keep class **$Properties
# If you do not use SQLCipher:
-dontwarn org.greenrobot.greendao.database.**
# If you do not use RxJava:
-dontwarn rx.**

#------ Dropbox -----
# OkHttp and Servlet optional dependencies
-dontwarn okio.**
-dontwarn okhttp3.**
-dontwarn com.squareup.okhttp.**
-dontwarn com.google.appengine.**
-dontwarn javax.servlet.**
# Support classes for compatibility with older API versions
-dontwarn android.support.**
-dontnote android.support.**

#------- Apache -------
-keep class org.apache.http.** { *; }
-keepclassmembers class org.apache.http.** {*;}
-dontwarn org.apache.**

#------ Drive API -------
-keep class * extends com.google.api.client.json.GenericJson {
*;
}
-keep class com.google.api.services.drive.** {
*;
}
-keepclassmembers class * { @com.google.api.client.util.Key <fields>; }
#------------------------

-keepclasseswithmembernames class * {
    native <methods>;
}
-keep class android.support.v8.renderscript.** { *; }
-keep class androidx.renderscript.** { *; }
-keep class com.fluidtouch.renderingengine.annotation.** { *; }


-keep class org.spongycastle.** { *; }
-dontwarn org.spongycastle.**

-dontwarn android.test.**
-dontwarn org.junit.**

-dontwarn javax.xml.bind.DatatypeConverter
-dontwarn org.apache.commons.codec.binary.Base64
-keep class com.myscript.iink.** { *; }
-keep class com.myscript.iink.graphics.** { *; }
-keep class com.myscript.iink.text.** { *; }
-keep class com.myscript.util.** { *; }
-keep class com.hianalytics.android.**{*;}
-keep class com.huawei.updatesdk.**{*;}
-keep class com.huawei.hms.**{*;}
-keep class com.myscript.iink.NativeLibrary.** { *; }
-keep class com.samsung.android.sdk.** { *; }
-keep class com.huawei.android.sdk.drm.**{*;}

-keep class org.benjinus.pdfium.** { *; }
-keep class com.tom_roush.** { *; }

