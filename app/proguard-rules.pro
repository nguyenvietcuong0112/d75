# ============================================================
# ProGuard Rules for D75 Theme App
# com.widget.themes.controlcenter
# ============================================================

# ---------------------- General ----------------------
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses,EnclosingMethod
-keepattributes Exceptions

# Keep the application class
-keep class com.widget.themes.controlcenter.** { *; }

# ---------------------- Kotlin ----------------------
-dontwarn kotlin.**
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
-keep class kotlin.reflect.** { *; }
-dontwarn kotlin.reflect.jvm.internal.**

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ---------------------- Kotlinx Serialization ----------------------
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    *** Companion;
}
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}
-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static ** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclasseswithmembers class **$$serializer {
    *** childSerializers(...);
}

# ---------------------- AndroidX / Lifecycle ----------------------
-keep class androidx.lifecycle.** { *; }
-dontwarn androidx.lifecycle.**
-keep class * extends androidx.lifecycle.ViewModel { <init>(...); }
-keep class * extends androidx.lifecycle.AndroidViewModel { <init>(...); }

# ---------------------- Room Database ----------------------
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**
-keep class * extends androidx.room.RoomDatabase { *; }
-keepclassmembers class * {
    @androidx.room.Dao *;
}

# ---------------------- Glide ----------------------
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule { <init>(...); }
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}
-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder$InternalRewinder {
    *** rewind();
}
-dontwarn com.bumptech.glide.**

# ---------------------- Firebase ----------------------
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# Firebase Crashlytics
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
-keep class com.google.firebase.crashlytics.** { *; }
-dontwarn com.google.firebase.crashlytics.**

# ---------------------- Google Mobile Ads (AdMob) ----------------------
-keep class com.google.android.gms.ads.** { *; }
-dontwarn com.google.android.gms.ads.**
-keep class com.google.ads.** { *; }

# ---------------------- Facebook SDK & Mediation ----------------------
-keep class com.facebook.** { *; }
-dontwarn com.facebook.**
-keepclassmembers class * implements java.io.Serializable {
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ---------------------- AppLovin Mediation ----------------------
-keep class com.applovin.** { *; }
-dontwarn com.applovin.**

# ---------------------- InMobi Mediation ----------------------
-keep class com.inmobi.** { *; }
-dontwarn com.inmobi.**
-keep class com.squareup.picasso.** { *; }

# PANGLE SDK
# ==============================================================================
-keep class com.bytedance.sdk.** { *; }
-dontwarn com.bytedance.sdk.**
-keep class com.bykv.vk.** { *; }
-dontwarn com.bykv.vk.**
-keep class com.google.ads.mediation.pangle.** { *; }

# ==============================================================================
# MINTEGRAL SDK
# ==============================================================================
-keep class com.mbridge.** { *; }
-dontwarn com.mbridge.**
-keep interface com.mbridge.** { *; }
-keep class * extends com.mbridge.msdk.out.BaseExtraInterfaceForHandler { *; }
-keep class com.google.ads.mediation.mintegral.** { *; }

# ---------------------- Unity Ads Mediation ----------------------
-keep class com.unity3d.ads.** { *; }
-dontwarn com.unity3d.ads.**
-keep class com.unity3d.services.** { *; }
-dontwarn com.unity3d.services.**

# ---------------------- Pubscale Adapter ----------------------
-keep class com.pubscale.** { *; }
-dontwarn com.pubscale.**

# ---------------------- CSC App Library ----------------------
-keep class com.cscapp.** { *; }
-dontwarn com.cscapp.**

# ---------------------- ExoPlayer / Media3 ----------------------
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**
-keep interface androidx.media3.common.Player { *; }

# ---------------------- Lottie ----------------------
-dontwarn com.airbnb.lottie.**
-keep class com.airbnb.lottie.** { *; }

# ---------------------- CircleIndicator ----------------------
-keep class me.relex.circleindicator.** { *; }

# ---------------------- RoundedImageView ----------------------
-keep class com.makeramen.roundedimageview.** { *; }

# ---------------------- NumberPicker ----------------------
-keep class com.shawnlin.numberpicker.** { *; }

# ---------------------- AndroidSVG ----------------------
-keep class com.caverock.androidsvg.** { *; }
-dontwarn com.caverock.androidsvg.**

# ---------------------- WorkManager ----------------------
-keep class androidx.work.** { *; }
-dontwarn androidx.work.**

# ---------------------- Play Services Location ----------------------
-keep class com.google.android.gms.location.** { *; }

# ---------------------- ViewBinding ----------------------
-keep class * implements androidx.viewbinding.ViewBinding {
    public static *** bind(android.view.View);
    public static *** inflate(...);
}

# ---------------------- Parcelize ----------------------
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# ---------------------- Enums ----------------------
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ---------------------- Serializable ----------------------
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ---------------------- WebView (if used) ----------------------
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# ---------------------- Native Methods ----------------------
-keepclasseswithmembernames class * {
    native <methods>;
}

# ---------------------- Browser / CustomTabs ----------------------
-keep class androidx.browser.** { *; }
-dontwarn androidx.browser.**

# ---------------------- SDP / SSP ----------------------
-keep class com.intuit.sdp.** { *; }
-keep class com.intuit.ssp.** { *; }

# ---------------------- Preference ----------------------
-keep class androidx.preference.** { *; }
-dontwarn androidx.preference.**