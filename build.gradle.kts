buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // Reproducible builds: pin R8 to a version that emits the DEX in a
        // deterministic byte order. The R8 bundled by AGP 8.5.2 is not
        // deterministic; 8.8.x is. See https://f-droid.org/docs/Reproducible_Builds/
        classpath("com.android.tools:r8:8.8.34")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
}
