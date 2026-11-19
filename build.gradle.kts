plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "ro.example.powerdisconnectcaller"
    compileSdk = 35
    defaultConfig {
        applicationId = "ro.example.powerdisconnectcaller"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "1.1"
    }
}
