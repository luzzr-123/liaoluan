plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "www.luuzr.liaoluan"
    compileSdk = 35

    defaultConfig {
        applicationId = "www.luuzr.liaoluan"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }

    signingConfigs {
        create("release") {
            storeFile = file("D:\\Sources\\keystore.jks")
            storePassword = "306619"
            keyAlias = "betone"
            keyPassword = "306619"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
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

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-P",
            "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=" + project.layout.buildDirectory.get().asFile.absolutePath + "/compose_metrics",
            "-P",
            "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=" + project.layout.buildDirectory.get().asFile.absolutePath + "/compose_metrics"
        )
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // Module Dependencies
    implementation(project(":core:database"))
    implementation(project(":core:designsystem"))
    implementation(project(":feature:habits"))
    implementation(project(":feature:notes"))
    implementation(project(":feature:stats"))

    // Compose BOM — 统一版本管理
    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)

    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)
    implementation(libs.compose.animation)
    implementation(libs.compose.foundation)

    // AndroidX 核心
    implementation(libs.core.ktx)
    implementation(libs.activity.compose)

    // Lifecycle
    implementation(libs.lifecycle.runtime)
    implementation(libs.lifecycle.viewmodel)

    // Navigation
    implementation(libs.navigation.compose)

    // Hilt — 依赖注入
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation)
    implementation(libs.hilt.work)
    ksp(libs.hilt.ext.compiler) // AndroidX Hilt Config

    // Room — 本地数据库
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Kotlin 扩展
    implementation(libs.kotlinx.serialization)
    implementation(libs.kotlinx.coroutines)

    // Coil — 图片加载
    implementation(libs.coil.compose)

    // WorkManager
    implementation(libs.work.runtime.ktx)

    // 调试工具
    debugImplementation(libs.compose.ui.tooling)
}
