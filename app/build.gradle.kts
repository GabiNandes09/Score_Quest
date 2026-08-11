plugins {

    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.rogue.scorequest"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.rogue.scorequest"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
}

dependencies {


    implementation(libs.androidx.core)

    implementation(libs.activity.compose)


    // Compose

    implementation(platform(libs.compose.bom))

    implementation(libs.androidx.material3)

    implementation(libs.androidx.material.icons.core)

    implementation(libs.androidx.material.icons.extended)

    implementation(libs.lifecycle.runtime.compose)


    // Koin

    implementation(libs.koin.android)

    implementation(libs.koin.compose)


    // Retrofit

    implementation(libs.retrofit)

    implementation(libs.retrofit.serialization)


    // OkHttp

    implementation(libs.okhttp)

    implementation(libs.okhttp.logging)


    // Serialization

    implementation(libs.kotlin.serialization)


    // Room

    implementation(libs.room.runtime)

    implementation(libs.room.ktx)

    implementation(libs.room.paging)

    ksp(libs.room.compiler)


    // CameraX

    implementation(libs.camera.core)

    implementation(libs.camera.camera2)

    implementation(libs.camera.lifecycle)

    implementation(libs.camera.view)


    // QR Code
    implementation(libs.mlkit.barcode)


    // WorkManager
    implementation(libs.workmanager)

    //Navigation
    implementation(libs.navigation.compose)

    //Jsoup
    implementation(libs.jsoup)
    implementation(libs.converter.scalars)

    // Paging
    implementation(libs.paging.runtime)
    implementation(libs.paging.compose)

    // DataStore
    implementation(libs.datastore.preferences)

    // Coil
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

}
