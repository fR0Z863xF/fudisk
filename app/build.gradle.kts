plugins {
    alias(libs.plugins.android.application)
    kotlin("android")
}

android {
    namespace = "com.fr0z863xf.fudisk"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.fR0Z863xF.fudisk"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"


    }

    splits {
        abi {
            reset()
            isEnable = true
            isUniversalApk = true
            include("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
        }
    }


    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    kotlin {
        jvmToolchain(17)
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            isJniDebuggable = false
            isDebuggable = false
        }
        getByName("debug") {
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        viewBinding = true
        compose = true
    }

//    applicationVariants.all { variant ->
//        variant.outputs.all { output ->
//            output.outputFileName.set("fudisk-${versionName}-${variant.buildType.name}-${abiName}.apk")
//        }
//    }
}

dependencies {

    implementation(libs.preference)
    implementation(libs.activity)
    val work_version = "2.9.1"


    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.24")
    implementation("androidx.compose.material3:material3:1.2.1")
    implementation("androidx.compose.material3:material3-window-size-class:1.2.1")
    implementation("androidx.compose.material3:material3-adaptive-navigation-suite:1.3.0-beta05")
    implementation("com.qcloud.cos:cos-android-nobeacon:5.9.+")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("androidx.work:work-runtime:$work_version")
    implementation("androidx.work:work-multiprocess:$work_version")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("androidx.datastore:datastore-preferences-rxjava2:1.1.1")
    implementation("com.google.code.gson:gson:2.11.+")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}