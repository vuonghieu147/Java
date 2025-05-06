import com.android.build.api.dsl.AaptOptions

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.smartscan"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.smartscan"
        minSdk = 24
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        mlModelBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.gridlayout)
    implementation(libs.camera.extensions)
    implementation(libs.litert)
    implementation(libs.litert.metadata)
    implementation(libs.litert.gpu)
    implementation(libs.litert.support.api)
    implementation(libs.firebase.crashlytics.buildtools)
    //implementation(libs.firebase.ml.vision)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    // To recognize Latin script
    implementation ("com.google.mlkit:text-recognition:16.0.1")
    // To recognize Chinese script
    implementation ("com.google.mlkit:text-recognition-chinese:16.0.1")
    // To recognize Devanagari script
    implementation ("com.google.mlkit:text-recognition-devanagari:16.0.1")
    // To recognize Japanese script
    implementation ("com.google.mlkit:text-recognition-japanese:16.0.1")
    // To recognize Korean script
    implementation ("com.google.mlkit:text-recognition-korean:16.0.1")
    val camerax_version = "1.5.0-alpha03"
    implementation("androidx.camera:camera-view:${camerax_version}")
    implementation("androidx.camera:camera-core:${camerax_version}")
    implementation("androidx.camera:camera-lifecycle:${camerax_version}")
    implementation ("androidx.camera:camera-camera2:${camerax_version}")
    implementation ("com.google.mlkit:translate:17.0.3")
    implementation ("com.google.android.gms:play-services-mlkit-language-id:17.0.0")
    implementation ("com.google.mlkit:linkfirebase:17.0.0")

    implementation("com.android.volley:volley:1.2.1")
    implementation ("com.google.code.gson:gson:2.13.1")

    implementation ("com.google.mlkit:image-labeling:17.0.9")
}
