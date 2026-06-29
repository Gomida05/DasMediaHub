import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.konan.properties.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.room3)
    alias(libs.plugins.dagger.hilt)
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
    compileSdk = 37

    defaultConfig {
        applicationId = "com.das.mediaHub"
        minSdk = 26
        targetSdk = 37
        versionCode = 16
        versionName = "16.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters.addAll(listOf("arm64-v8a", "x86_64"))
        }
    }


    dependenciesInfo {
        includeInBundle = false
        includeInApk = false
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }


    buildFeatures {
        compose = true
        buildConfig = true
    }


}

kotlin {

    jvmToolchain(21)
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
        allWarningsAsErrors.set(false)
        freeCompilerArgs.add("-opt-in=androidx.compose.material3.ExperimentalMaterial3Api")
    }
}

room3 {
    schemaDirectory("$projectDir/schemas")
}

dependencies {

    // Python Integration: The app uses a dedicated :python module
    // for metadata scraping and extraction via Chaquopy.
//     implementation(project(":python")) // Uncomment for development
//     implementation(project(":downloader"))
    implementation(libs.com.das.downloader)
    implementation(libs.com.das.python)
//    implementation(files("../libs/aar/das-python.aar"))
//    implementation(files("../libs/aar/das-downloader.aar"))

    //Ktor
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)

    //Firebase dependencies
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.firestore)

    implementation(libs.coil.compose)
    implementation(libs.coil.video)


    //WorkManager
    implementation(libs.androidx.work.runtime.ktx)
    //Material 3
    implementation(platform(libs.compose.bom))
    implementation(libs.material3)
    implementation(libs.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)

    //Room DB
    implementation(libs.androidx.room3.runtime)
    ksp(libs.androidx.room3.compiler)

    //Dagger Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    //preview
    implementation(libs.ui.tooling.preview)
    debugImplementation(libs.ui.tooling)

    //icons
    implementation(libs.material.icons.extended)

    //DataStore
    implementation(libs.androidx.datastore.preferences)

    //browser
    implementation(libs.browser)


    implementation(libs.core.ktx)
    implementation(libs.media)

    implementation(libs.glide)
    implementation(libs.runtime.android)


    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.navigation3.runtime)

    //Media 3
    implementation(libs.media3.session)
    implementation(libs.media3.ui)
    implementation(libs.media3.ui.compose)
    implementation(libs.media3.ui.material3)
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.common.ktx)

    //serialization JSON
    implementation(libs.kotlinx.serialization.json)



}