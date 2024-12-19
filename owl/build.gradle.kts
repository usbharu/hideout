import kotlinx.kover.gradle.plugin.dsl.CoverageUnit

plugins {
    alias(libs.plugins.kotlin.jvm)
    id("maven-publish")
    alias(libs.plugins.kover)
    alias(libs.plugins.detekt)
}


allprojects {
    group = "dev.usbharu"
    version = "0.0.2-SNAPSHOT"


    repositories {
        mavenCentral()
    }
}

tasks {
    create("publishMavenPublicationToMavenLocal") {
        subprojects.forEach { dependsOn("${it.path}:publishMavenPublicationToMavenLocal") }
    }
    create("publishMavenPublicationToGiteaRepository") {
        subprojects.forEach { dependsOn("${it.path}:publishMavenPublicationToGiteaRepository") }
    }
}

subprojects {
    apply {
        plugin("org.jetbrains.kotlin.jvm")
        plugin("maven-publish")
        plugin(rootProject.libs.plugins.kover.get().pluginId)
        plugin(rootProject.libs.plugins.detekt.get().pluginId)
    }
    kotlin {
        jvmToolchain(21)
    }

    dependencies {
        implementation("org.slf4j:slf4j-api:2.0.15")
        testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
        detektPlugins(rootProject.libs.detekt.formatting)

    }

    detekt {
        parallel = true
        config.setFrom(files("$rootDir/../detekt.yml"))
        buildUponDefaultConfig = true
        basePath = "${projectDir}/src/main/kotlin"
        autoCorrect = true
    }

    project.gradle.taskGraph.whenReady {
        if (this.hasTask(":koverGenerateArtifact")) {
            val task = this.allTasks.find { it.name == "test" }
            val verificationTask = task as VerificationTask
            verificationTask.ignoreFailures = true
        }
    }
    tasks {
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
        withType<Test> {
            useJUnitPlatform()
        }
    }

    publishing {
        repositories {
            maven {
                name = "Gitea"
                url = uri("https://git.usbharu.dev/api/packages/usbharu/maven")

                credentials(HttpHeaderCredentials::class) {
                    name = "Authorization"
                    value = "token " + (project.findProperty("gpr.gitea") as String? ?: System.getenv("GITEA"))
                }

                authentication {
                    create<HttpHeaderAuthentication>("header")
                }
            }
        }

        publications {
            register<MavenPublication>("maven") {
                groupId = "dev.usbharu"
                artifactId = project.name
                version = project.version.toString()
                from(components["kotlin"])
            }
        }
    }
}

dependencies {
    kover(project(":owl-broker"))
    kover(project(":owl-broker:owl-broker-mongodb"))
    kover(project(":owl-common"))
    kover(project(":owl-common:owl-common-serialize-jackson"))
    kover(project(":owl-consumer"))
    kover(project(":owl-producer"))
    kover(project(":owl-producer:owl-producer-api"))
    kover(project(":owl-producer:owl-producer-default"))
    kover(project(":owl-producer:owl-producer-embedded"))
}

detekt {
    parallel = true
    config.setFrom(files("../detekt.yml"))
    buildUponDefaultConfig = true
    basePath = "${projectDir}/src/main/kotlin"
    autoCorrect = true
}

project.gradle.taskGraph.whenReady {
    if (this.hasTask(":koverGenerateArtifact")) {
        val task = this.allTasks.find { it.name == "test" }
        val verificationTask = task as VerificationTask
        verificationTask.ignoreFailures = true
    }
}

kover {
    currentProject {
        sources {
            excludedSourceSets.addAll("grpc", "grpckt")
        }
    }
    reports {
        verify {
            rule {
                bound {
                    minValue = 50
                    coverageUnits = CoverageUnit.INSTRUCTION
                }
            }
        }
        total {
            xml {
                title = "Owl"
                xmlFile = file("$buildDir/reports/kover/owl.xml")
            }
            filters {
                excludes {
                    packages("dev.usbharu.owl.generated")
                }
            }
        }


    }
}
