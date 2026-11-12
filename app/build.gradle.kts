plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "pe.novatick.anitec"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "pe.novatick.anitec"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:18080/api/v1/\"")
            buildConfigField("String", "API_ENVIRONMENT", "\"LAB\"")
        }
        create("alpha") {
            initWith(getByName("debug"))
            applicationIdSuffix = ".alpha.linking"
            versionNameSuffix = "-alpha-linking-f4"
            buildConfigField("String", "API_BASE_URL", "\"https://anitec-staging.onrender.com/api/v1/\"")
            buildConfigField("String", "API_ENVIRONMENT", "\"STAGING\"")
        }
        create("labAlpha") {
            initWith(getByName("debug"))
            applicationIdSuffix = ".alpha.lab"
            versionNameSuffix = "-alpha-lab-rc1"
            buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:18080/api/v1/\"")
            buildConfigField("String", "API_ENVIRONMENT", "\"LAB\"")
        }
        release {
            buildConfigField("String", "API_BASE_URL", "\"https://anitec-staging.onrender.com/api/v1/\"")
            buildConfigField("String", "API_ENVIRONMENT", "\"STAGING\"")
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

androidComponents {
    beforeVariants(selector().withBuildType("alpha")) { builder ->
        (builder as com.android.build.api.variant.HasHostTestsBuilder).hostTests.getValue("UnitTest").enable = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Phase 1: HTTP/JSON, observable lifecycle state and targeted session tests.
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.10.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")

    // Mis implementaciones
    // Navegación en Jetpack Compose
    implementation(libs.androidx.navigation.compose)
    implementation(libs.coil.compose)
}
