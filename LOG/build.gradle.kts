plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}

group = "com.github.jitpack"
version = "0.1.3"

android {
    namespace = "com.xplor.log"
    compileSdk = 33

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        // make the Android Test Orchestrator run the 'pm clear' command after each test invocation
        testInstrumentationRunnerArguments["clearPackageData"] = "true"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    testOptions {
        animationsDisabled = true
        unitTests.isIncludeAndroidResources = true
        unitTests.isReturnDefaultValues = true

        // the Android Test Orchestrator makes tests run in separate containers
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("org.slf4j:slf4j-nop:2.0.7")
    implementation("com.jakewharton.timber:timber:5.0.1")

    testImplementation("org.robolectric:robolectric:4.10.3")
    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.test:runner:1.5.2")
    testImplementation("androidx.test.ext:truth:1.5.0")
    testImplementation("io.mockk:mockk:1.13.5")

    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestUtil("androidx.test:orchestrator:1.4.2")
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "marianatek-android-log-lib"
            artifactId = "marianatek-android-log-lib"
            version = "0.1.0"

            afterEvaluate {
                from(components["release"])
            }
        }
    }
    repositories {
        maven {
            name = "android-log"
            url = uri("${project.buildDir}/repo")
        }
    }
}
