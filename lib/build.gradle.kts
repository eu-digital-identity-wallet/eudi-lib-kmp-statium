import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import org.jetbrains.dokka.DokkaConfiguration
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.net.URI

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.spotless)
    alias(libs.plugins.dependency.check)
    alias(libs.plugins.kover)
    alias(libs.plugins.sonarqube)
    alias(libs.plugins.dokka)
}

repositories {
    mavenCentral()
    google()
}

spotless {
    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**/*.kt")
        ktlint(libs.versions.ktlint.get())
            .editorConfigOverride(
                mapOf(
                    "ktlint_standard_filename" to "disabled",
                    "ktlint_standard_no-wildcard-imports" to "disabled",
                ),
            )
        trimTrailingWhitespace()
        licenseHeaderFile("../FileHeader.txt")
        endWithNewline()
    }
    kotlinGradle {
        target("**/*.gradle.kts")
        ktlint(libs.versions.ktlint.get())
        trimTrailingWhitespace()
        endWithNewline()
    }
}

kotlin {
    explicitApiWarning()
    jvmToolchain(libs.versions.java.get().toInt())

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        optIn =
            listOf(
                "kotlinx.serialization.ExperimentalSerializationApi",
                "kotlin.contracts.ExperimentalContracts",
                "kotlin.io.encoding.ExperimentalEncodingApi",
            )
        freeCompilerArgs =
            listOf(
                "-Xconsistent-data-class-copy-visibility",
            )
    }

    // JVM target
    jvm()

    // Android target
    androidTarget {
        // Set JVM target to 17 to match Java compatibility
        // Using direct property access instead of deprecated kotlinOptions
        JvmTarget.fromTarget(libs.versions.java.get())
            .let { javaTarget ->
                compilations.all {
                    compileTaskProvider.configure {
                        compilerOptions.jvmTarget.set(javaTarget)
                    }
                }
            }
    }

    // Set up targets
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyDefaultHierarchyTemplate {
        // create a new group that depends on `common`
        common {
            // Define group name without `Main` as suffix
            group("jvmAndAndroid") {
                // Provide which targets would be part of this group
                withJvm()
                withAndroidTarget()
            }
        }
    }

    // Configure source sets
    sourceSets {
        commonMain {
            dependencies {
                // Common dependencies
                api(libs.kotlinx.serialization.core)
                api(libs.kotlinx.serialization.json)
                api(libs.kotlinx.datetime)
                api(libs.kotlinx.coroutines.core)
                api(libs.ktor.client.core)
                implementation(libs.ktor.client.serialization)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.json)
            }
        }

        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.ktor.client.mock)
            }
        }

        jvmMain {
            dependencies {
                implementation(libs.ktor.client.java)
            }
        }

        jvmTest {
            dependencies { }
        }

        @Suppress("unused")
        val jvmAndAndroidMain by getting {
            dependencies { }
        }

        @Suppress("unused")
        val jvmAndAndroidTest by getting {
            dependencies { }
        }

        androidMain {
            dependencies {
                implementation(libs.ktor.client.okhttp)
            }
        }

        androidUnitTest {
            dependencies { }
        }
    }
}

// Android configuration
android {
    namespace = properties["namespace"].toString()
    group = properties["group"].toString()
    compileSdk = properties["android.targetSdk"].toString().toInt()

    defaultConfig {
        minSdk = properties["android.minSdk"].toString().toInt()
    }

    compileOptions {
        JavaVersion.toVersion(libs.versions.java.get().toInt())
            .let { javaVersion ->
                sourceCompatibility = javaVersion
                targetCompatibility = javaVersion
            }
    }
}

dependencyCheck {
    formats = listOf("XML", "HTML")
    nvd.apiKey = System.getenv("NVD_API_KEY") ?: properties["nvdApiKey"]?.toString() ?: ""
    nvd.delay = 10000
    nvd.maxRetryCount = 2
}

mavenPublishing {
    configure(
        KotlinMultiplatform(
            javadocJar = JavadocJar.Dokka(tasks.dokkaHtml.name),
            sourcesJar = true,
            androidVariantsToPublish = listOf("release"),
        ),
    )

    coordinates(
        groupId = properties["group"].toString(),
        artifactId = rootProject.name,
        version = properties["version"].toString(),
    )

    pom {
        ciManagement {
            system = "github"
            url = "${project.properties["POM_SCM_URL"]}/actions"
        }
    }
}

tasks.register("jacocoTestReport") {
    dependsOn(tasks.koverXmlReport)
}

tasks.sonarqube {
    dependsOn(tasks.koverXmlReport)
}

sonarqube {
    properties {
        val report = "${project.layout.buildDirectory.asFile.get().absolutePath}/reports/kover/report.xml"
        property("sonar.coverage.jacoco.xmlReportPaths", report)
    }
}

tasks.sonar {
    dependsOn(tasks.koverXmlReport)
}

sonar {
    properties {
        val report = "${project.layout.buildDirectory.asFile.get().absolutePath}/reports/kover/report.xml"
        property("sonar.coverage.jacoco.xmlReportPaths", report)
    }
}

tasks.withType<DokkaTask>().configureEach {
    dokkaSourceSets {
        configureEach {
            // used as project name in the header
            moduleName.set(properties["POM_NAME"].toString())
            moduleVersion.set(project.version.toString())

            // contains descriptions for the module and the packages
            // includes.from("Module.md")

            documentedVisibilities.set(
                setOf(
                    DokkaConfiguration.Visibility.PUBLIC,
                    DokkaConfiguration.Visibility.PROTECTED,
                ),
            )

            val remoteSourceUrl =
                System.getenv()["GIT_REF_NAME"]?.let {
                    URI.create("${properties["POM_SCM_URL"]}/tree/$it/${project.layout.projectDirectory.asFile.name}/src").toURL()
                }
            remoteSourceUrl
                ?.let {
                    sourceLink {
                        localDirectory.set(projectDir.resolve("src"))
                        remoteUrl.set(it)
                        remoteLineSuffix.set("#L")
                    }
                }
        }
    }
}
