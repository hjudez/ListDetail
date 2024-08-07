plugins {
    kotlin("kapt")
    id("com.google.devtools.ksp")
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.hexterlabs.listdetail"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.hexterlabs.listdetail"
        minSdk = 31
        //noinspection EditedTargetSdkVersion
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "FOURSQUARE_CLIENT_ID", "\"" + project.findProperty("foursquare_client_id") + "\"")
        buildConfigField("String", "FOURSQUARE_CLIENT_SECRET", "\"" + project.findProperty("foursquare_client_secret") + "\"")

        testInstrumentationRunner = "com.hexterlabs.listdetail.CustomTestRunner"
    }

    buildFeatures {
        compose = true
        buildConfig = true
        //noinspection DataBindingWithoutKapt
        dataBinding = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.7"
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

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1,LICENSE.md,LICENSE-notice.md}"
        }
    }

    testOptions {
        packaging {
            jniLibs {
                useLegacyPackaging = true
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // Compose
    val composeBom = platform("androidx.compose:compose-bom:2023.10.01")
    implementation(composeBom)

    // Material Design 3
    implementation("androidx.compose.material3:material3:1.2.1")

    // Core Jetpack
    implementation("androidx.core:core-ktx:1.13.1")

    // Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    // Logging for lazy people
    implementation("com.jakewharton.timber:timber:5.0.1")

    // Database
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-common:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")

    // Glide image downloading
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // Dependency Injection
    implementation("com.google.dagger:hilt-android:2.50")
    kapt("com.google.dagger:hilt-android-compiler:2.50")

    // Retrofit
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")

    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

    // Testing
    kaptAndroidTest("com.google.dagger:hilt-android-compiler:2.50")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("io.mockk:mockk-android:1.13.8")
    androidTestImplementation("androidx.test:core:1.6.1")
    androidTestImplementation("androidx.test:runner:1.6.1")
    androidTestImplementation("androidx.test:rules:1.6.1")
    androidTestImplementation("androidx.arch.core:core-testing:2.2.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.50")
}

kapt {
    correctErrorTypes = true
}