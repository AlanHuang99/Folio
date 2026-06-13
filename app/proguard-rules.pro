# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.folio.reader.data.api.models.** { *; }
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

# Gson
-keepattributes Signature
-keep class com.google.gson.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

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
