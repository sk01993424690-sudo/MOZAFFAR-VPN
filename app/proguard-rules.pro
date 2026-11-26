# WireGuard Android Tunnel JNI & Config classes
-keep class com.wireguard.** { *; }
-dontwarn com.wireguard.**

# Moshi & Retrofit
-keepattributes Signature,InnerClasses,EnclosingMethod
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-keep,allowobfuscation,allowshrinking class kotlin.Metadata { *; }

# Google Mobile Ads & Play Billing
-keep class com.google.android.gms.ads.** { *; }
-keep class com.android.billingclient.** { *; }
