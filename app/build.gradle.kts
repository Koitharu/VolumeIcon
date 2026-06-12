plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "org.koitharu.volumeicon"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "org.koitharu.volumeicon"
        minSdk = 24
        targetSdk = 36
        versionCode = 5
        versionName = "1.4"
    }

    lint {
        disable += "AccessibilityPolicy"
        disable += "MissingTranslation"
        disable += "UseRequiresApi" // not available without AndroidX libs
    }

    buildTypes {
        release {
            optimization {
                enable = true
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    dependenciesInfo {
        includeInApk = false
    }
}

dependencies {
    // no dependencies :)
}