# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.folio.reader.data.api.models.** { *; }
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

# Gson
-keepattributes Signature
-keep class com.google.gson.** { *; }
# Account is Gson-serialized into the accounts DataStore; keep its field names
# stable so stored accounts survive R8 renaming and app updates.
-keep class com.folio.reader.data.Account { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# readability4j pulls in slf4j-api, which references an optional logging binding
# that isn't bundled (it falls back to no-op at runtime). Don't fail R8 over it.
-dontwarn org.slf4j.**

# WorkManager instantiates workers by class name via reflection; keep the
# subclass and its (Context, WorkerParameters) constructor so R8 doesn't break sync.
-keep class * extends androidx.work.ListenableWorker {
    <init>(android.content.Context, androidx.work.WorkerParameters);
}

# Reproducible builds: R8's ServiceLoader optimization builds a static service
# list whose order can vary between builds, breaking byte-for-byte
# reproducibility. Keep the coroutines ServiceLoader interfaces so R8 leaves the
# lookup alone. See https://f-droid.org/docs/Reproducible_Builds/
-keep class kotlinx.coroutines.CoroutineExceptionHandler
-keep class kotlinx.coroutines.internal.MainDispatcherFactory
