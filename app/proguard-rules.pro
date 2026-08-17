# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in the default Android SDK proguard-android-optimize.txt file.

# ── Hilt ─────────────────────────────────────────────────────────────────────
-keep class dagger.hilt.** { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }

# ── Room ──────────────────────────────────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }

# ── DataStore ─────────────────────────────────────────────────────────────────
-keep class androidx.datastore.** { *; }

# ── Kotlin Serialization ──────────────────────────────────────────────────────
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}

# ── Block me specific ─────────────────────────────────────────────────────────
-keep class com.blockme.app.service.** { *; }
-keep class com.blockme.core.** { *; }
-keepclassmembers class * extends android.app.admin.DeviceAdminReceiver { *; }
-keepclassmembers class * extends android.accessibilityservice.AccessibilityService { *; }
