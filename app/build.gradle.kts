plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
}

android {
  namespace = "com.starrynightstudio.dezhmessenger.xwpqrs"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.starrynightstudio.dezhmessenger.xwpqrs"
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  // --- تنظیمات تفکیک ساخت APKها بر اساس معماری (ABI Splits) ---
  splits {
    abi {
      isEnable = true
      reset()
      // لیست معماری‌های هدف
      include("arm64-v8a", "armeabi-v7a", "x86_64", "x86")
      // فعال‌سازی ساخت APK کلی و همه‌کاره (Universal)
      isUniversalApk = true
    }
  }

  signingConfigs {
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
      val keystoreFile = file(keystorePath)
      storeFile = keystoreFile
      storePassword = System.getenv("STORE_PASSWORD") ?: "dezh_prod_pass"
      keyAlias = "upload"
      keyPassword = System.getenv("KEY_PASSWORD") ?: "dezh_prod_pass"
    }
    create("debugConfig") {
      storeFile = file("${rootDir}/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
    debug {
      signingConfig = signingConfigs.getByName("debugConfig")
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
  testOptions { unitTests { isIncludeAndroidResources = true } }
}

// اختصاص خودکار versionCode اختصاصی به هر معماری جهت جلوگیری از تداخل موقع نصب/انتشار
androidComponents {
  onVariants { variant ->
    variant.outputs.forEach { output ->
      val abiFilter = output.filters.find { it.filterType == com.android.build.api.variant.FilterConfiguration.FilterType.ABI }?.identifier
      val abiCodes = mapOf(
        "armeabi-v7a" to 1,
        "arm64-v8a" to 2,
        "x86" to 3,
        "x86_64" to 4
      )
      if (abiFilter != null && abiCodes.containsKey(abiFilter)) {
        val baseCode = android.defaultConfig.versionCode ?: 1
        output.versionCode.set(baseCode * 1000 + abiCodes[abiFilter]!!)
      }
    }
  }
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
}

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(platform(libs.firebase.bom))
  // implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  // implementation(libs.androidx.camera.camera2)
  // implementation(libs.androidx.camera.core)
  // implementation(libs.androidx.camera.lifecycle)
  // implementation(libs.androidx.camera.view)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  // implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.coil.compose)
  implementation(libs.converter.moshi)
  // implementation(libs.firebase.ai)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  // implementation(libs.play.services.location)
  implementation(libs.retrofit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  implementation(libs.androidx.security.crypto)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
}

tasks.register("generateReleaseKeystore") {
  val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
  val keystoreFile = file(keystorePath)
  val path = keystoreFile.absolutePath
  val endWithJks = keystorePath.endsWith("my-upload-key.jks")

  doLast {
    val fileOnDisk = File(path)
    if (!fileOnDisk.exists() && endWithJks) {
      println("Generating persistent release keystore...")
      try {
        val process = ProcessBuilder(
          "keytool", "-genkeypair",
          "-alias", "upload",
          "-keyalg", "RSA",
          "-keysize", "2048",
          "-validity", "10000",
          "-keystore", fileOnDisk.absolutePath,
          "-storepass", "dezh_prod_pass",
          "-keypass", "dezh_prod_pass",
          "-dname", "CN=Prod, O=Dezh, C=US"
        ).start()
        val exitCode = process.waitFor()
        if (exitCode == 0) {
          println("Persistent release keystore generated successfully at ${fileOnDisk.absolutePath}")
        } else {
          System.err.println("Failed to generate release keystore. exitCode: $exitCode")
        }
      } catch (e: java.lang.Exception) {
        e.printStackTrace()
      }
    } else {
      println("Release keystore exists or non-default path configured: $path")
    }
  }
}

tasks.configureEach {
  if (name.startsWith("package") || name.startsWith("validateSigning")) {
    dependsOn("generateReleaseKeystore")
  }
}