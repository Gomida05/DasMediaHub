import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.konan.properties.Properties
import java.io.FileInputStream
import kotlin.apply

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.chaquo.python)
    alias(libs.plugins.kotlin.serialization)
    id("maven-publish")
}

private val loadLocalProperties = Properties().apply {
    load(FileInputStream(rootProject.file("local.properties")))
}

android {
    namespace = "com.das.python"

    defaultConfig {
        minSdk = 26
        compileSdk = 37
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        ndk {
            abiFilters.addAll(listOf("arm64-v8a", "x86_64"))
        }
    }
    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

kotlin {
    jvmToolchain(21)

    compilerOptions {
        jvmTarget.set(
            JvmTarget.JVM_21
        )
    }

}
publishing {
    publications {
        create<MavenPublication>("release") {
            groupId = "com.das"
            artifactId = "python"
            version = "1.0.0"

            afterEvaluate {
                from(components["release"])
            }
        }
    }
}
chaquopy {
    defaultConfig {
        version = "3.14"
        buildPython(loadLocalProperties["PYTHON_PATH"] as String)

        pip {
            install("yt-dlp")
            install("pytubefix==8.12.1")
//            install("youtube-search-python")
            install("httpx<0.28")
            install("requests")
        }
    }
    sourceSets {
        getByName("main"){
            srcDir("src/main/python")
        }
    }
}
dependencies {
//    implementation(libs.core.ktx)
//    implementation(libs.appcompat)
//    testImplementation(libs.junit)
    implementation(libs.kotlinx.coroutines.android)
    //serialization JSON
    implementation(libs.kotlinx.serialization.json)
}

