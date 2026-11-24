import java.util.Properties

val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

/** Escapa comillas y barras para inyectar secretos en BuildConfig sin romper el Kotlin generado. */
fun String.paraBuildConfig(): String = replace("\\", "\\\\").replace("\"", "\\\"")

fun propiedad(nombre: String, defecto: String = ""): String =
    localProps.getProperty(nombre, defecto).takeIf { it.isNotBlank() }
        ?: defecto
        .paraBuildConfig()

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example.remedia"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.remedia"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "SOCRATA_KEY_ID", "\"${propiedad("SOCRATA_KEY_ID")}\"")
        buildConfigField("String", "SOCRATA_KEY_SECRET", "\"${propiedad("SOCRATA_KEY_SECRET")}\"")

        // Gemini es OPCIONAL: sin GEMINI_API_KEY la app usa solo el normalizador
        // heurÃ­stico y no hace ninguna peticiÃ³n extra.
        buildConfigField("String", "GEMINI_API_KEY", "\"${propiedad("GEMINI_API_KEY")}\"")
        buildConfigField(
            "String",
            "GEMINI_MODEL",
            "\"${propiedad("GEMINI_MODEL", "gemini-3.1-flash-lite")}\""
        )
        buildConfigField(
            "String",
            "GEMINI_BASE_URL",
            "\"${propiedad("GEMINI_BASE_URL", "https://generativelanguage.googleapis.com/")}\""
        )
    }

    buildTypes {
        release {
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

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.mlkit.text.recognition)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging.interceptor)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}