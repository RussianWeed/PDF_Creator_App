plugins {
    id("com.android.application")
}

android {
    namespace = "com.devking.pdf_v3"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.devking.pdf_v3"
        minSdk = 34
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        renderscriptTargetApi = 30
        renderscriptSupportModeEnabled = true
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
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.camera:camera-view:1.3.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    implementation ("com.itextpdf:itextpdf:5.5.13.2")

    // CameraX Libraries
    implementation ("androidx.camera:camera-core:1.2.0-alpha01")
    implementation ("androidx.camera:camera-camera2:1.2.0-alpha01")
    implementation ("androidx.camera:camera-lifecycle:1.2.0-alpha01")
    implementation ("androidx.camera:camera-view:1.0.0-alpha27")


}