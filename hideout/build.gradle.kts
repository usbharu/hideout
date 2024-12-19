plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.kotlin.spring)
}

apply {
    plugin("io.spring.dependency-management")
}


group = "dev.usbharu.hideout"
version = "unspecified"

allprojects {
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

tasks {
    register("run") {
        dependsOn(gradle.includedBuild("hideout-core").task(":run"))
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