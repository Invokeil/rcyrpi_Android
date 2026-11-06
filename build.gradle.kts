// Root build file — RCY-RPI Android app (package: cfd.invokeil.himadri.rcy)
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false

    // ── FCM (enable later) ────────────────────────────────────────────────
    // To enable push notifications:
    //   1. Uncomment the google-services plugin in gradle/libs.versions.toml
    //   2. Uncomment the line below
    //   3. Drop google-services.json into app/
    //   4. Uncomment the FCM lines in app/build.gradle.kts
    // alias(libs.plugins.google.services) apply false
}
