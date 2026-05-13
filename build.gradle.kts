// Top-level build file

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false

    // ✅ Correct way to add Google Services plugin in Kotlin DSL
    id("com.google.gms.google-services") version "4.4.0" apply false
}