import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    // Kotlin serialization plugin for type safe routes and navigation arguments
    kotlin("plugin.serialization") version "2.0.21"

    id ("kotlin-kapt")
}

android {
    namespace = "com.ities45.skycast"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ities45.skycast"
        minSdk = 28
        targetSdk = 35
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
        getByName("debug") {
            buildConfigField("String", "OPENWEATHER_API_KEY", "\"${getLocalProperty("OPENWEATHER_API_KEY")}\"")
        }
        getByName("release") {
            buildConfigField("String", "OPENWEATHER_API_KEY", "\"${getLocalProperty("OPENWEATHER_API_KEY")}\"")
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
        viewBinding = true
        buildConfig = true
    }
}

fun getLocalProperty(key: String): String {
    val properties = Properties()
    // Use project.rootDir to point to the project root
    val localPropertiesFile = file("${project.rootDir}/local.properties")
    return if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { stream ->
            properties.load(stream)
            properties.getProperty(key) ?: ""
        }
    } else {
        "" // Return empty string if file is missing
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    val nav_version = "2.9.0"

    // Jetpack Compose integration
    implementation("androidx.navigation:navigation-compose:$nav_version")

    // Views/Fragments integration
    implementation("androidx.navigation:navigation-fragment:$nav_version")
    implementation("androidx.navigation:navigation-ui:$nav_version")

    // Feature module support for Fragments
    implementation("androidx.navigation:navigation-dynamic-features-fragment:$nav_version")

    // Testing Navigation
    androidTestImplementation("androidx.navigation:navigation-testing:$nav_version")

    // JSON serialization library, works with the Kotlin serialization plugin
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    //Coroutines Dependencies
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.4.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")

    //Retrofit
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")

    //GSON
    implementation ("com.google.code.gson:gson:2.10.1")

    //Room
    implementation ("androidx.room:room-ktx:2.6.1")
    implementation ("androidx.room:room-runtime:2.6.1")
    kapt ("androidx.room:room-compiler:2.6.1")

    implementation ("org.jetbrains.kotlin:kotlin-stdlib:1.8.22")

    //ViewModel & livedata
    implementation ("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1")
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.5.1")

    //Ktx
    implementation ("androidx.activity:activity-ktx:1.5.0")
    implementation ("androidx.fragment:fragment-ktx:1.6.2")

    //Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")

    //okhttp
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    //RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.4.0")

    //WorkManager
    implementation ("androidx.work:work-runtime-ktx:2.9.1")

    //Google Play Services Location
    implementation("com.google.android.gms:play-services-location:21.1.0")

    //SDP
    implementation ("com.intuit.sdp:sdp-android:1.1.1")

    //SSP
    implementation ("com.intuit.ssp:ssp-android:1.1.1")

    //Lottie Airbnb
    implementation ("com.airbnb.android:lottie:6.6.6")

    //Android Security Crypto
    implementation ("androidx.security:security-crypto:1.0.0")

    //Open Street Map
    implementation ("org.osmdroid:osmdroid-android:6.1.20")
}