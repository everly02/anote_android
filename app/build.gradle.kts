plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    kotlin("kapt")
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
}

dependencies {

    implementation(libs.androidx.material3.android)
    implementation(libs.androidx.media3.common)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    val appcompat_version = "1.6.1"

    implementation("androidx.appcompat:appcompat:$appcompat_version")
    // For loading and tinting drawables on older versions of the platform
    implementation("androidx.appcompat:appcompat-resources:$appcompat_version")
    implementation (libs.kotlin.stdlib.jdk7)

    // 核心库
    implementation (libs.androidx.core.ktx.v160)

    // Compose UI 工具包
    implementation (libs.androidx.ui)
    // Material Design 主题的 Compose 支持
    implementation (libs.androidx.material)

    implementation (libs.material)
    implementation ("androidx.compose.material:material-icons-core:1.6.4")

    implementation("androidx.compose.material:material-icons-extended:1.6.4")
    // Compose UI 工具包预览功能
    implementation ("androidx.compose.ui:ui-tooling-preview:1.6.4")
    // Compose 运行时 LiveData 支持
    implementation ("androidx.compose.runtime:runtime-livedata:1.6.4")
    // Compose 导航
    implementation ("androidx.navigation:navigation-compose:2.7.7")

    // Activity Compose 支持
    implementation ("androidx.activity:activity-compose:1.8.2")

    // ViewModel Compose 支持
    implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    // 协程
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    // 测试
    androidTestImplementation ("androidx.compose.ui:ui-test-junit4:1.6.4")
    debugImplementation ("androidx.compose.ui:ui-tooling:1.6.4")
    //noinspection UseTomlInstead

    val room_version = "2.6.1"


    implementation ("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")
    // To use Kotlin Symbol Processing (KSP)
    kapt("androidx.room:room-compiler:$room_version")
    // Kotlin Extensions and Coroutines support for Room
    implementation ("androidx.room:room-ktx:$room_version")

    // Testing Room
    testImplementation ("androidx.room:room-testing:$room_version")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    // LiveData
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    // Lifecycle Runtime
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    // Coroutine支持的LiveData
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    implementation("androidx.media3:media3-exoplayer:1.3.0")
    implementation("androidx.media3:media3-exoplayer-dash:1.3.0")
    implementation("androidx.media3:media3-ui:1.3.0")
}