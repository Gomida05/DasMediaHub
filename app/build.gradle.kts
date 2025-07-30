import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.konan.properties.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.chaquo.python)
    alias(libs.plugins.google.gms)

    //ksp
    alias(libs.plugins.kotlin.devtools.ksp)
}

private val loadLocalProperties = Properties().apply {
    load(FileInputStream(rootProject.file("local.properties")))
}
android {
    signingConfigs {
        create("release") {
            // Replace with your own details in the local.properties file
            storeFile = file(loadLocalProperties["KEYSTORE_FILE"] as String)
            storePassword = loadLocalProperties["KEYSTORE_PASSWORD"] as String
            keyAlias = loadLocalProperties["KEY_ALIAS"] as String
            keyPassword = loadLocalProperties["KEY_PASSWORD"] as String
        }
    }
    namespace = "com.das.forui"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.das.forui"
        minSdk = 24
        targetSdk = 36
        versionCode = 3
        versionName = "1.23"

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
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }


    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            freeCompilerArgs.add("-opt-in=androidx.compose.material3.ExperimentalMaterial3Api")
        }
    }


    buildFeatures {
        viewBinding = true
        compose = true
    }


}


chaquopy {
    defaultConfig {
        version = "3.13"
        buildPython(loadLocalProperties["PYTHON_PATH"] as String)

        pip {
            install("pytubefix==8.12.1")
            install("youtube-search-python")
            install("httpx<0.28")
        }
    }
    sourceSets {
        getByName("main"){
            srcDir("src/main/python")
        }
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {

    //Room
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)

    //Firebase dependencies
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)

    implementation(libs.play.services.auth)

    implementation(libs.lottie.compose)
    implementation(libs.coil.compose)
    implementation(libs.coil.video)

    implementation(libs.runtime.livedata)
    implementation(libs.navigation.compose)
    implementation(libs.ui.viewbinding)

    //Material 3
    implementation(platform(libs.compose.bom))
    implementation(libs.material3)


    implementation(libs.activity.compose)

    //preview
    debugImplementation(libs.ui.tooling)

    //icons
    implementation(libs.material.icons.extended)



    implementation(libs.lifecycle.viewmodel.compose)

    implementation(libs.core.ktx)
    implementation(libs.media)


    implementation(libs.gson)

    implementation(libs.glide)
    implementation(libs.runtime.android)
    implementation(libs.appcompat)


    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.ui.ktx)

    //Media 3
    implementation(libs.media3.session)
    implementation(libs.media3.exoplayer.dash)
    implementation(libs.media3.exoplayer.hls)
    implementation(libs.media3.ui)
    implementation(libs.media3.ui.compose)
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.common)
    implementation(libs.media3.common.ktx)


    testImplementation(libs.junit)
    implementation(libs.kotlin.stdlib)
}