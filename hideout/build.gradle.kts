plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.detekt)
}

apply {
    plugin("io.spring.dependency-management")
}




allprojects {

    group = "dev.usbharu.hideout"
    version = "1.0.0-SNAPSHOT"

    repositories {
        mavenCentral()
        maven {
            url = uri("https://git.usbharu.dev/api/packages/usbharu/maven")
        }
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/usbharu/http-signature")
            credentials {

                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
            }
        }
        maven {
            name = "GitHubPackages2"
            url = uri("https://maven.pkg.github.com/multim-dev/emoji-kt")
            credentials {

                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
            }
        }
    }
}

subprojects {
    apply {
        plugin(rootProject.libs.plugins.kotlin.jvm.get().pluginId)
        plugin(rootProject.libs.plugins.detekt.get().pluginId)
    }

    dependencies {
        testImplementation(kotlin("test"))
        detektPlugins(rootProject.libs.detekt.formatting)
    }
}

tasks {
    register("run") {
        dependsOn(gradle.includedBuild("hideout-core").task(":run"))
    }
    withType<io.gitlab.arturbosch.detekt.Detekt> {
        exclude("**/generated/**")
        setSource("src/main/kotlin")
        exclude("build/")
        configureEach {
            exclude("**/org/koin/ksp/generated/**", "**/generated/**")
        }
    }
    withType<io.gitlab.arturbosch.detekt.DetektCreateBaselineTask>() {
        configureEach {
            exclude("**/org/koin/ksp/generated/**", "**/generated/**")
        }
    }

    kotlin {
        jvmToolchain(21)
    }
}

dependencies {
    implementation(project(":hideout-core"))
    implementation(project(":hideout-mastodon"))
    implementation(project(":hideout-activitypub"))
}

springBoot {
    buildInfo { }

    mainClass = "dev.usbharu.hideout.SpringApplicationKt"
}

configurations {
    matching { it.name == "detekt" }.all {
        resolutionStrategy.eachDependency {
            if (requested.group == "org.jetbrains.kotlin") {
                useVersion(io.gitlab.arturbosch.detekt.getSupportedKotlinVersion())
            }
        }
    }
}

detekt {
    parallel = true
    config.setFrom(files("../detekt.yml"))
    buildUponDefaultConfig = true
    basePath = "${rootDir.absolutePath}/src/main/kotlin"
    autoCorrect = true
}