import java.io.FileInputStream
import java.util.Properties
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21"
}

// --- VERSIONING START ---
val versionPropsFile = file("version.properties")
val versionProps = Properties()

if (versionPropsFile.canRead()) {
    versionProps.load(FileInputStream(versionPropsFile))
} else {
    throw GradleException("Could not find version.properties in app directory!")
}

// Read values safely (Default to 0 or 1 if missing)
val vMajor = versionProps["VERSION_MAJOR"]?.toString()?.toInt() ?: 1
val vMinor = versionProps["VERSION_MINOR"]?.toString()?.toInt() ?: 0
val vPatch = versionProps["VERSION_PATCH"]?.toString()?.toInt() ?: 0
val vBuild = versionProps["VERSION_BUILD"]?.toString()?.toInt() ?: 1
// --- VERSIONING END ---

android {
    namespace = "org.umoja4life.drilltutor"
    compileSdk = 36

    defaultConfig {
        applicationId = "org.umoja4life.drilltutor"
        minSdk = 24
        targetSdk = 36
        versionCode = vBuild
        versionName = "$vMajor.$vMinor.$vPatch"

        buildConfigField("String", "DRILLTUTOR_VERSION_CODE", "\"$vBuild\"")
        buildConfigField("String", "DRILLTUTOR_MY_VERSION", "\"V$versionName\"")

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
        resValues = true
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
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

// --- BUMP TASKS ---
fun updateVersion(majorInc: Int, minorInc: Int, patchInc: Int) {
    val newMajor = if (majorInc > 0) vMajor + majorInc else vMajor
    val newMinor = if (majorInc > 0) 0 else (if (minorInc > 0) vMinor + minorInc else vMinor)
    val newPatch = if (majorInc > 0 || minorInc > 0) 0 else vPatch + patchInc
    val newBuild = vBuild + 1

    versionProps["VERSION_MAJOR"] = newMajor.toString()
    versionProps["VERSION_MINOR"] = newMinor.toString()
    versionProps["VERSION_PATCH"] = newPatch.toString()
    versionProps["VERSION_BUILD"] = newBuild.toString()

    versionProps.store(versionPropsFile.writer(), null)
    println("Version bumped to: $newMajor.$newMinor.$newPatch (Build $newBuild)")
}

tasks.register("bumpPatch") { doLast { updateVersion(0, 0, 1) } }
tasks.register("bumpMinor") { doLast { updateVersion(0, 1, 0) } }
tasks.register("bumpMajor") { doLast { updateVersion(1, 0, 0) } }

