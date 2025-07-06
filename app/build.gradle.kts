import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.chaquo.python)
    alias(libs.plugins.google.gms)
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
    compileSdk = 36

    defaultConfig {
        applicationId = "com.das.forui"
        minSdk = 24
        targetSdk = 36
        versionCode = 3
        versionName = "1.21"

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
//            allWarningsAsErrors.set(false)
            freeCompilerArgs.add("-opt-in=androidx.compose.material3.ExperimentalMaterial3Api")
//            freeCompilerArgs.add("-Xlint:deprecation")
        }
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

        pip {
            install("pytubefix")
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



dependencies {


    //Firebase dependencies
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.storage)

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
    implementation(libs.ui.tooling.preview)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)
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