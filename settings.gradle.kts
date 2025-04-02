//
// Enables declaring module dependencies in a safer manner
// Instead using
//      implementation(project(":core:cache"))
// You can use
//      implementation(projects.core.cache)
//
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    //
    // Provides a repository for downloading JVMs
    //
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}


dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        google()
        mavenCentral()
        mavenLocal()
    }
}

rootProject.name = "eudi-lib-kmp-statium"
include(":lib")
