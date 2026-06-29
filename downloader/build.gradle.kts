import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    id("maven-publish")
    id("kotlin-parcelize")
}

android {


    defaultConfig {
        namespace = "com.das.downloader"
        compileSdk = 37
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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

    publishing {
        singleVariant("release") {
            withSourcesJar()
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
            artifactId = "downloader"
            version = "2.0.0"

            afterEvaluate {
                from(components["release"])
            }
        }
    }
}



dependencies {

    implementation(libs.com.das.python)

    //Ktor
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)

    //serialization JSON
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.core.ktx)
    implementation(libs.androidx.work.runtime.ktx)

}