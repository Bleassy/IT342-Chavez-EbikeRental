# Retrofit
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# OkHttp
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# Gson
-keep class com.google.gson.** { *; }
-keep interface com.google.gson.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# Data models
-keep class com.ebike.mobile.data.models.** { *; }
-keep class com.ebike.mobile.data.models.**$** { *; }

# Auth related
-keep class com.auth0.** { *; }
-keep interface com.auth0.** { *; }

# Google Auth
-keep class com.google.android.gms.** { *; }
-keep interface com.google.android.gms.** { *; }

# Kotlin
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }

# Coroutines
-keep class kotlinx.coroutines.** { *; }

# Logging
-keep class com.jakewharton.timber.** { *; }
-keep interface com.jakewharton.timber.** { *; }

# DataStore
-keep class androidx.datastore.** { *; }
-keep interface androidx.datastore.** { *; }

# Compose
-keep class androidx.compose.** { *; }
-keep interface androidx.compose.** { *; }

# Lifecycle
-keep class androidx.lifecycle.** { *; }
-keep interface androidx.lifecycle.** { *; }

# Generic signature of Keep
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-dontnote sun.misc.**
-dontnote sun.reflect.**
-dontnote android.annotation.**
-dontnote android.support.**

# Application classes that will be serialized/deserialized over Gson
-keep class com.ebike.mobile.data.** { *; }

# Prevent proguard from stripping interface information from TypeAdapterFactory,
# JsonSerializer and JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
