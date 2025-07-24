

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0" // ✅ Required for Kotlin 2.0+
    id("com.google.gms.google-services")
    kotlin("plugin.serialization") version "1.9.22"
}

android {
    namespace = "com.anxietystressselfmanagement"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.anxietystressselfmanagement"
        minSdk = 26
        targetSdk = 35
        versionCode = 20
        versionName = "2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    packaging {
        resources {
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/LICENSE"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/NOTICE"
            excludes += "META-INF/NOTICE.txt"
        }
    }

}

dependencies {
    implementation (libs.mpandroidchart)
    implementation (platform(libs.firebase.bom.v3210)) // Check for the latest version
    implementation (libs.google.firebase.auth.ktx)
    implementation (libs.google.firebase.firestore.ktx)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.androidx.navigation.compose.jvmstubs)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation (libs.material.v130alpha03)
    implementation (libs.glide)
    annotationProcessor (libs.compiler)
    implementation (libs.material.v130alpha03)
    implementation (libs.glide)
    annotationProcessor(libs.compiler)
    implementation(libs.okhttp)
    implementation(libs.moshi)
    implementation(libs.firebase.vertexai)
    implementation (libs.google.auth.library.oauth2.http)
    implementation (libs.play.services.auth)
    implementation (libs.firebase.auth.v2310)
    implementation (libs.play.services.base.v1820)
    implementation (libs.androidx.work.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.kotlinx.coroutines.android)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(platform("androidx.compose:compose-bom:2024.04.01")) // Add BOM manually
    implementation(libs.bundles.compose)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")


    // Optional tools:
    debugImplementation("androidx.compose.ui:ui-tooling:1.6.5")
}