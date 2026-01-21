import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "org.umoja4life.drilltutor"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "org.umoja4life.drilltutor"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "v0.0.1"

        buildConfigField("String", "DRILLTUTOR_VERSION_CODE", "\"1\"")
        buildConfigField("String", "DRILLTUTOR_MY_VERSION", "\"V0.1\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val buildDateStamp = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
        buildConfigField("String", "BUILD_TIME", "\"$buildDateStamp\"")
        resValue("string", "build_time", buildDateStamp)
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}