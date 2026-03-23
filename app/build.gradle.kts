import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.konan.properties.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
//    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms)
    alias(libs.plugins.kotlin.serialization)

    id("kotlin-parcelize")
}

private val loadLocalProperties = Properties().apply {
    load(FileInputStream(rootProject.file("local.properties")))
}
android {
    signingConfigs {
        create("release") {
            // Please make sure to replace with your own details in the local.properties file
            storeFile = file(loadLocalProperties["KEYSTORE_FILE"] as String)
            storePassword = loadLocalProperties["KEYSTORE_PASSWORD"] as String
            keyAlias = loadLocalProperties["KEY_ALIAS"] as String
            keyPassword = loadLocalProperties["KEY_PASSWORD"] as String
        }
    }
    namespace = "com.das.mediaHub"

    defaultConfig {
        applicationId = "com.das.mediaHub"
        compileSdk = 36
        minSdk = 26
        targetSdk = 36
        versionCode = 9
        versionName = "1.29"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        flavorDimensions.add("pyVersion")
        ndk {
            abiFilters.addAll(listOf("arm64-v8a", "x86_64"))
        }
    }


    dependenciesInfo {
        includeInBundle = false
        includeInApk = false
    }
    buildToolsVersion = "36.1.0"

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_18
        targetCompatibility = JavaVersion.VERSION_18
    }


    buildFeatures {
        compose = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_18)
        freeCompilerArgs.add("-opt-in=androidx.compose.material3.ExperimentalMaterial3Api")
    }
}


dependencies {

    // Python Integration: The app uses a dedicated :python module
    // for metadata scraping and extraction via Chaquopy.
    // implementation(project(":python")) // Uncomment for development
    implementation(files("libs/aar/python-release.aar"))


    //Firebase dependencies
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.firestore.ktx)


    //Google
    implementation(libs.play.services.auth)

    implementation(libs.coil.compose)
    implementation(libs.coil.video)

    implementation(libs.runtime.livedata)
    implementation(libs.ui.viewbinding)

    //Material 3
    implementation(platform(libs.compose.bom))
    implementation(libs.material3)


    implementation(libs.activity.compose)

    //preview
    implementation(libs.ui.tooling.preview)
    implementation(libs.googleid)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.ui.tooling)

    //icons
    implementation(libs.material.icons.extended)

    //browser
    implementation(libs.browser)


    implementation(libs.core.ktx)
    implementation(libs.media)

    implementation(libs.glide)
    implementation(libs.runtime.android)
    implementation(libs.appcompat)


    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.navigation3.runtime)

    //Media 3
    implementation(libs.media3.session)
    implementation(libs.media3.exoplayer.dash)
    implementation(libs.media3.exoplayer.hls)
    implementation(libs.media3.ui)
    implementation(libs.media3.ui.compose)
    implementation(libs.media3.ui.material3)
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.common)
    implementation(libs.media3.common.ktx)

    //serialization JSON
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)

    testImplementation(libs.junit)
    implementation(libs.kotlin.stdlib)
    debugImplementation(libs.androidx.ui.test.manifest)

    //okhttp
    implementation(libs.okhttp)

}