plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.chaquo.python") version "16.0.0"
    id("org.jetbrains.kotlin.plugin.compose")
}


android {
    signingConfigs {
        create("py312Release") {
            storeFile = file("C:\\Users\\esrom\\AndroidStudioProjects\\VideoDownloader\\app\\myRelease.jks")
            storePassword = "Esrom@11"
            keyAlias = "key1"
            keyPassword = "Esrom@11"
        }
    }
    namespace = "com.das.forui"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.das.forui"
        minSdk = 24
        targetSdk = 35
        versionCode = 2
        versionName = "1.14"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        flavorDimensions.add("pyVersion")
        ndk {
            abiFilters.addAll(listOf("arm64-v8a", "x86_64"))
        }


    }




    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("py312Release")
        }
    }



    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = freeCompilerArgs + "-Xlint:deprecation"
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

    implementation("io.coil-kt:coil-compose:2.2.2")
    implementation("io.coil-kt:coil-video:2.4.0")
    implementation("androidx.compose.runtime:runtime-livedata:1.8.0")
    implementation("androidx.navigation:navigation-compose:2.8.9")
    implementation("androidx.compose.ui:ui-viewbinding:1.8.0")

    //Material 3

    implementation(platform("androidx.compose:compose-bom:2025.04.01"))
    implementation("androidx.compose.material3:material3")


    implementation("androidx.activity:activity-compose:1.10.1")

    //preview
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    //icons
    implementation("androidx.compose.material:material-icons-extended")



    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.media:media:1.7.0")
//    implementation("androidx.appcompat:appcompat:1.7.0")

    implementation("com.google.code.gson:gson:2.10.1")

    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("androidx.compose.runtime:runtime-android:1.8.0")
    implementation("androidx.appcompat:appcompat:1.7.0")


    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.9")
    implementation("androidx.media3:media3-session:1.6.1")
    implementation("androidx.media3:media3-exoplayer-dash:1.6.1")
    implementation("androidx.media3:media3-exoplayer-hls:1.6.1")

    implementation("androidx.media3:media3-ui-compose:1.6.1")
    implementation("androidx.media3:media3-exoplayer:1.6.1")
    implementation("androidx.media3:media3-common:1.6.1")
    implementation("androidx.media3:media3-common-ktx:1.6.1")
    testImplementation("junit:junit:4.13.2")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.1.0")
}