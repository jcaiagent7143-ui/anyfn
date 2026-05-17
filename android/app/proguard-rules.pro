# Keep generated serializers from kotlinx-serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keep,includedescriptorclasses class dev.anyfn.**$$serializer { *; }
-keepclassmembers class dev.anyfn.** {
    *** Companion;
}
-keepclasseswithmembers class dev.anyfn.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Hilt
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel
-keepclassmembers class * {
    @dagger.hilt.android.lifecycle.HiltViewModel <init>(...);
}

# Room
-keep class * extends androidx.room.RoomDatabase
-keep class * extends androidx.room.Entity
-keep @androidx.room.Entity class *

# Ktor
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**
-dontwarn org.slf4j.**

# AccessibilityService is referenced from XML/manifest
-keep class dev.anyfn.accessibility.AnyfnAccessibilityService { *; }
