plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.jamirodev.myline"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.jamirodev.myline"
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
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
    }

}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")

    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-firestore-ktx:24.10.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    // Material
    implementation("com.google.android.material:material:1.10.0")

    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:32.6.0"))

    // TODO: Add the dependencies for Firebase products you want to use
    // When using the BoM, don't specify versions in Firebase dependencies
    implementation("com.google.firebase:firebase-analytics")

    // Add the dependency for the Firebase Authentication library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-auth")

    // Add the dependency for the Realtime Database library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-database")

    // Also add the dependency for the Google Play services library and specify its version
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    //Glade for images (read images from firebase in this case)
    implementation ("com.github.bumptech.glide:glide:4.16.0")

    // Code picker for phone codes
    implementation ("com.hbb20:ccp:2.7.0")

    // Add the dependency for the Cloud Storage library
    implementation("com.google.firebase:firebase-storage")

    // Maps SDK for Android
    implementation ("com.google.android.gms:play-services-maps:18.2.0")

    // Places
    implementation ("com.google.android.libraries.places:places:3.3.0" )

    // BaseFlow PhotoViewer
    implementation ("com.github.chrisbanes:PhotoView:2.3.0")

    // Notifications firebase
    implementation ("com.google.firebase:firebase-messaging-ktx:23.4.0")

    // SDK Google Ads
    implementation ("com.google.android.gms:play-services-ads:22.5.0")


}