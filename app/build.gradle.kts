plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
    id("kotlin-kapt")
    id("kotlin-parcelize")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.example.fixproblem"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.fixproblem"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

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
        viewBinding = true
    }
}

dependencies {
    // Firebase Cloud Messaging
    implementation ("com.google.firebase:firebase-messaging-ktx:24.1.0")
    implementation ("androidx.hilt:hilt-work:1.2.0")

    // Work Manager để handle background tasks
    implementation ("androidx.work:work-runtime-ktx:2.9.0")
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    implementation ("androidx.room:room-ktx:$room_version")
    implementation ("androidx.room:room-paging:$room_version")
    ksp("androidx.room:room-compiler:$room_version")
    implementation("androidx.exifinterface:exifinterface:1.3.6")
    val paging_version = "3.2.1"
    // Paging 3
    implementation ("androidx.paging:paging-runtime:$paging_version")
    implementation ("androidx.paging:paging-compose:$paging_version")
    implementation("com.mapbox.search:mapbox-search-android:2.7.0")
    implementation("com.mapbox.maps:android:11.9.0")
    implementation("com.mapbox.extension:maps-compose:11.9.0")
    implementation("com.mapbox.search:autofill:2.7.0")
    implementation("com.mapbox.search:discover:2.7.0")
    implementation("com.mapbox.search:place-autocomplete:2.7.0")
    implementation("com.mapbox.navigationcore:android:3.7.0-beta.1")
    implementation ("com.mapbox.navigationcore:ui-components:3.7.0-beta.1")
    implementation(libs.googleid)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    val credential_ver = "1.3.0"
    implementation ("androidx.credentials:credentials:$credential_ver")
    implementation ("androidx.credentials:credentials-play-services-auth:$credential_ver")
    //implementation ("com.google.android.libraries.identity.googleid:$credential_ver")
    // Dagger Hilt
    implementation ("com.google.dagger:hilt-android:2.52")
    implementation(libs.firebase.storage.ktx)
    kapt ("com.google.dagger:hilt-compiler:2.52")
    // AndroidX Hilt Integration
    implementation ("androidx.hilt:hilt-navigation-compose:1.2.0")
    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))

    // When using the BoM, you don't specify versions in Firebase library dependencies

    // Add the dependency for the Firebase SDK for Google Analytics
    implementation("com.google.firebase:firebase-analytics")

    // TODO: Add the dependencies for any other Firebase products you want to use
    // See https://firebase.google.com/docs/android/setup#available-libraries
    // For example, add the dependencies for Firebase Authentication and Cloud Firestore
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation ("androidx.constraintlayout:constraintlayout-compose:1.1.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation ("io.coil-kt:coil-compose:2.0.0")
    implementation ("com.google.accompanist:accompanist-permissions:0.30.1")
    implementation ("com.google.accompanist:accompanist-systemuicontroller:0.30.1")
    val camerax_version = "1.3.0-alpha04"
    implementation ("androidx.camera:camera-core:$camerax_version")
    implementation ("androidx.camera:camera-camera2:$camerax_version")
    implementation ("androidx.camera:camera-lifecycle:$camerax_version")
    implementation ("androidx.camera:camera-view:$camerax_version")
    implementation("androidx.navigation:navigation-compose:2.8.5")
    implementation("androidx.compose.runtime:runtime:1.7.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.navigation.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}