
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.chaquo.python") version "16.0.0"
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.firebase.firebase-perf")
}


android {
//    signingConfigs {
//        getByName("debug") {
//            storeFile = file("C:\\Users\\esrom\\my-release-key.jks")
//            storePassword = "Esrom@11"
//            keyAlias = "2005"
//            keyPassword = "Esrom@11"
//        }
//    }
    namespace = "com.das.forui"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.das.forui"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.11"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        flavorDimensions.add("pyVersion")
        ndk {
            abiFilters.addAll(listOf("arm64-v8a", "x86_64"))
        }


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



    kotlinOptions {
        jvmTarget = "1.8"
    }


    buildFeatures {
        viewBinding = true
        compose = true
    }


    productFlavors {
        create("py312") {
            dimension = "pyVersion"
            version = "3.13"
        }
    }


}
chaquopy {
    defaultConfig {
        version = "3.13"
        buildPython("C:\\Users\\esrom\\AppData\\Local\\Programs\\Python\\Python313\\python.exe")
        pip{
            install("pytubefix")
            install("youtube-search-python")
            install("httpx<0.28")
        }
    }
    sourceSets {
        getByName("main"){
            srcDir("src/main/python") }
    }

}



dependencies {


    //ImageLoader

    implementation("io.coil-kt:coil-compose:2.2.2")
    implementation("androidx.media3:media3-session:1.5.1")

    //Material 3
    val composeBom = platform("androidx.compose:compose-bom:2025.01.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.compose.material3:material3")


    implementation("androidx.activity:activity-compose:1.10.1")

    //preview
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    //icons
    implementation("androidx.compose.material:material-icons-extended")

    //ui test
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    implementation("com.facebook.shimmer:shimmer:0.5.0")
    implementation("com.google.android.exoplayer:exoplayer:2.19.1")
    implementation("com.google.android.exoplayer:exoplayer-dash:2.19.1")
    implementation("com.google.android.exoplayer:exoplayer-hls:2.19.1")
    implementation("com.google.android.exoplayer:exoplayer-ui:2.19.1")
    implementation("com.google.android.exoplayer:extension-mediasession:2.19.1")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.media:media:1.7.0")
    implementation("androidx.appcompat:appcompat:1.7.0")

    implementation("com.google.code.gson:gson:2.10.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation(platform("com.google.firebase:firebase-bom:33.11.0"))
    implementation("com.google.firebase:firebase-analytics")
    
    implementation("com.google.firebase:firebase-perf")

    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("androidx.compose.runtime:runtime-android:1.7.8")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
//    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.9")
    //noinspection GradleDependency
    implementation("androidx.navigation:navigation-ui-ktx:2.8.4")
    implementation("androidx.work:work-runtime-ktx:2.10.0")
    implementation("com.google.firebase:firebase-database-ktx:21.0.0")
    implementation("androidx.media3:media3-exoplayer:1.5.1")
    testImplementation("junit:junit:4.13.2")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.1.0")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
