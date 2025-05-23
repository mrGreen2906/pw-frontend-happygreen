plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.frontend_happygreen"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.frontend_happygreen"
        minSdk = 25
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "OPENAI_API_KEY", "\"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\"")
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
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")

    // Gson for JSON parsing
    implementation ("com.google.code.gson:gson:2.10.1")
    implementation("io.coil-kt:coil-compose:2.4.0")
    // Coroutines for async operations
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Lifecycle components
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    // OSMDroid per mappe open source
    implementation("org.osmdroid:osmdroid-android:6.1.16")

// Google Play Services Location
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // Material Icons Extended per icone aggiuntive
    implementation("androidx.compose.material:material-icons-extended:1.5.4")

// ViewModel per Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")

    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // Gson per la serializzazione JSON
    implementation ("com.google.code.gson:gson:2.10.1")

    implementation ("androidx.lifecycle:lifecycle-runtime-compose:2.6.2")

    // Coil per caricamento immagini (opzionale)

    // ML Kit per barcode scanning (modello bundled)
    implementation("com.google.mlkit:barcode-scanning:17.3.0")

    // CameraX
    implementation("androidx.camera:camera-camera2:1.3.0")
    implementation("androidx.camera:camera-lifecycle:1.3.0")
    implementation("androidx.camera:camera-view:1.3.0")

    implementation("com.airbnb.android:lottie-compose:6.2.0")

    implementation (libs.androidx.material.icons.core)
    implementation (libs.androidx.material.icons.extended)
    implementation("io.coil-kt:coil-compose:2.4.0") // o l'ultima versione disponibile

    // For API calls
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:okhttp:4.11.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // Coroutines
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")

    implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")



    implementation("androidx.compose.ui:ui:1.6.0")
    implementation("androidx.compose.foundation:foundation:1.6.0")
    implementation("androidx.compose.material3:material3:1.2.0")


    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.play.services.mlkit.barcode.scanning)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.lifecycle.process)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}