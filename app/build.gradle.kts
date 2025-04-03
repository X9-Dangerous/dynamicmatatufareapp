import java.util.Properties

// Load the .env file from the root directory
val dotenv = Properties().apply {
    val envFile = rootProject.file(".env")
    if (envFile.exists()) {
        load(envFile.inputStream())
    }
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
    id("kotlin-kapt")
}

android {
    namespace = "com.example.dynamic_fare"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.dynamic_fare"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Use values from .env (falling back to test values if not found)
        buildConfigField("String", "MPESA_CONSUMER_KEY", "\"${dotenv["MPESA_CONSUMER_KEY"] ?: project.findProperty("MPESA_CONSUMER_KEY") ?: "TEST_CONSUMER_KEY"}\"")
        buildConfigField("String", "MPESA_CONSUMER_SECRET", "\"${dotenv["MPESA_CONSUMER_SECRET"] ?: project.findProperty("MPESA_CONSUMER_SECRET") ?: "TEST_CONSUMER_SECRET"}\"")
        buildConfigField("String", "BUSINESS_SHORT_CODE", "\"${dotenv["BUSINESS_SHORT_CODE"] ?: project.findProperty("BUSINESS_SHORT_CODE") ?: "TEST_BUSINESS_SHORT_CODE"}\"")
        buildConfigField("String", "PASSKEY", "\"${dotenv["PASSKEY"] ?: project.findProperty("PASSKEY") ?: "TEST_PASSKEY"}\"")
        buildConfigField("String", "CALLBACK_URL", "\"${dotenv["CALLBACK_URL"] ?: project.findProperty("CALLBACK_URL") ?: "https://test-callback-url.com/mpesa"}\"")
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("androidx.navigation:navigation-compose:2.8.9")
    implementation("com.google.firebase:firebase-database-ktx:21.0.0")
    implementation("com.google.zxing:core:3.5.2")
    implementation("com.google.android.gms:play-services-auth:21.3.0")
    implementation("com.google.firebase:firebase-auth-ktx:23.2.0")
    implementation("com.google.firebase:firebase-firestore-ktx:25.1.3")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("io.coil-kt:coil-compose:2.4.0")
    implementation("com.google.mlkit:barcode-scanning:17.2.0")
    implementation("androidx.camera:camera-camera2:1.4.2")
    implementation("androidx.camera:camera-core:1.4.2")
    implementation("androidx.camera:camera-lifecycle:1.4.2")
    implementation("androidx.camera:camera-view:1.4.2")
    implementation("com.squareup.retrofit2:adapter-rxjava3:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    implementation("androidx.datastore:datastore-preferences:1.1.4")
    implementation("androidx.compose.material3:material3:1.3.1")
    implementation("com.google.android.gms:play-services-mlkit-barcode-scanning:18.3.1")
    implementation("com.google.guava:guava:32.1.3-android")
    implementation("com.google.android.gms:play-services-code-scanner:16.1.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("org.osmdroid:osmdroid-android:6.1.10")
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.1")
    debugImplementation("androidx.compose.ui:ui-tooling:1.5.1")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.5.1")
}
