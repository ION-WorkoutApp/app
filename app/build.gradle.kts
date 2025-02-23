plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

android {
    namespace = "com.ion606.workoutapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ion606.workoutapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "0.0.7-alpha"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("boolean", "SENSITIVE_LOGGING_ENABLED", "true")
            applicationIdSuffix = ".debug"
        }

        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("boolean", "SENSITIVE_LOGGING_ENABLED", "false")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    configurations {
        implementation {
            exclude(group = "org.jetbrains", module = "annotations")
        }
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.lifecycle.process)
    val work_version = "2.10.0"

    implementation(libs.material3)
    implementation(libs.androidx.material)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.okhttp)
    implementation(libs.gson)
    implementation(libs.okhttp.v4110)
    implementation(libs.gson.v289)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.appcompat)
    implementation(libs.mockito.core)
    implementation(libs.androidx.security.crypto.v100rc02)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.compiler)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.room.ktx)
    implementation(libs.mpandroidchart)
    implementation(libs.compose)
    implementation("androidx.work:work-runtime-ktx:$work_version") // DO NOT CHANGE TO LIB FUNC
    ksp(libs.room.compiler)
    ksp(libs.androidx.room.ktx)
    testImplementation(libs.junit)
    testImplementation(libs.bytebuddy.byte.buddy)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core.v311)
    androidTestImplementation(libs.androidx.core)
    androidTestImplementation(libs.androidx.junit.v110)
    androidTestImplementation(libs.support.annotations)
    androidTestImplementation(libs.runner)
    androidTestImplementation(libs.rules)
    testImplementation(libs.androidx.work.testing)
    testImplementation(libs.androidx.core)
    androidTestImplementation(libs.guava)
    implementation(libs.guava.v290android)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

// ksp-2.0.21-1.0.27 is too new for kotlin-2.0.0. Please upgrade kotlin-gradle-plugin to 2.0.21.