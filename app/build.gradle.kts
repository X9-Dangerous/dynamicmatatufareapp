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
}

android {
    namespace = "com.example.dynamic_fare"
    compileSdk = 35
    namespace = "com.example.dynamic_fare"

    defaultConfig {
        applicationId = "com.example.dynamic_fare"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Use values from .env (falling back to test values if not found)
        buildConfigField("String", "MPESA_CONSUMER_KEY", "\"${dotenv["MPESA_CONSUMER_KEY"] ?: "TEST_CONSUMER_KEY"}\"")
        buildConfigField("String", "MPESA_CONSUMER_SECRET", "\"${dotenv["MPESA_CONSUMER_SECRET"] ?: "TEST_CONSUMER_SECRET"}\"")
        buildConfigField("String", "BUSINESS_SHORT_CODE", "\"${dotenv["BUSINESS_SHORT_CODE"] ?: "TEST_BUSINESS_SHORT_CODE"}\"")
        buildConfigField("String", "PASSKEY", "\"${dotenv["PASSKEY"] ?: "TEST_PASSKEY"}\"")
        buildConfigField("String", "CALLBACK_URL", "\"${dotenv["CALLBACK_URL"] ?: "https://test-callback-url.com/mpesa"}\"")
    }

    buildTypes {
        /*debug {
            // Optional: For debug builds, you can override values if needed.
            buildConfigField("String", "MPESA_CONSUMER_KEY", "\"${dotenv["MPESA_CONSUMER_KEY"] ?: "TEST_CONSUMER_KEY"}\"")
            buildConfigField("String", "MPESA_CONSUMER_SECRET", "\"${dotenv["MPESA_CONSUMER_SECRET"] ?: "TEST_CONSUMER_SECRET"}\"")
            buildConfigField("String", "BUSINESS_SHORT_CODE", "\"${dotenv["BUSINESS_SHORT_CODE"] ?: "TEST_BUSINESS_SHORT_CODE"}\"")
            buildConfigField("String", "PASSKEY", "\"${dotenv["PASSKEY"] ?: "TEST_PASSKEY"}\"")
            buildConfigField("String", "CALLBACK_URL", "\"${dotenv["CALLBACK_URL"] ?: "https://test-callback-url.com/mpesa"}\"")
        }*/
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
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("androidx.navigation:navigation-compose:2.8.9")
    implementation("com.google.firebase:firebase-database-ktx:21.0.0")
    implementation("com.google.zxing:core:3.4.1")
    implementation("com.google.android.gms:play-services-auth:21.3.0")
    implementation("com.google.firebase:firebase-auth-ktx:23.2.0")
    implementation("com.google.firebase:firebase-firestore-ktx:25.1.3")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.firebase.messaging.ktx)
    implementation("org.osmdroid:osmdroid-android:6.1.10")
    implementation(libs.datatransport.transport.api)
    implementation(libs.datatransport.transport.api)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    val nav_version = "2.8.9"
    implementation("androidx.navigation:navigation-compose:$nav_version")
}