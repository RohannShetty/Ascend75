# ProGuard / R8 Rules for Ascend 75

# Room Database
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keep class androidx.room.RoomDatabase$Callback { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>();
}

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Kotlinx Serialization
-keepattributes *Annotation*,InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}

# AndroidX Security Crypto & Keystore
-keep class androidx.security.crypto.** { *; }
-dontwarn androidx.security.crypto.**

# Hilt & Dagger
-keep class com.google.crypto.tink.** { *; }
-keep class * extends dagger.hilt.internal.GeneratedComponent { *; }
-keep class * extends androidx.hilt.work.WorkerFactory { *; }
-keepclassmembers class * {
    @javax.inject.Inject <init>(...);
}
