import kotlinx.kover.gradle.plugin.dsl.CoverageUnit
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.openapi.generator)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kover)
}


apply {
    plugin("io.spring.dependency-management")
}


dependencies {
    detektPlugins(libs.detekt.formatting)
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    implementation(project(":hideout-core"))

    implementation(libs.jackson.databind)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.jakarta.annotation)
    implementation(libs.jakarta.validation)

    implementation(libs.bundles.exposed)
    implementation(libs.bundles.openapi)
    implementation(libs.bundles.coroutines)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation(libs.bundles.spring.boot.oauth2)
    testImplementation(libs.kotlin.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.h2db)
    testImplementation(libs.flyway.core)
    testImplementation(libs.http.signature)
    testRuntimeOnly(libs.flyway.postgresql)
}


tasks {
    test {
        useJUnitPlatform()
    }

    compileKotlin {
        dependsOn("openApiGenerateMastodonCompatibleApi")
        mustRunAfter("openApiGenerateMastodonCompatibleApi")
    }

    create<GenerateTask>("openApiGenerateMastodonCompatibleApi") {
        generatorName.set("kotlin-spring")
        inputSpec.set("$projectDir/src/main/resources/openapi/mastodon.yaml")
        outputDir.set("$buildDir/generated/sources/mastodon")
        apiPackage.set("dev.usbharu.hideout.mastodon.interfaces.api.generated")
        modelPackage.set("dev.usbharu.hideout.mastodon.interfaces.api.generated.model")
        configOptions.put("interfaceOnly", "true")
        configOptions.put("useSpringBoot3", "true")
        configOptions.put("reactive", "true")
        configOptions.put("gradleBuildFile", "false")
        configOptions.put("useSwaggerUI", "false")
        configOptions.put("enumPropertyNaming", "UPPERCASE")
        additionalProperties.put("useTags", "true")

        importMappings.put("org.springframework.core.io.Resource", "org.springframework.web.multipart.MultipartFile")
        typeMappings.put("org.springframework.core.io.Resource", "org.springframework.web.multipart.MultipartFile")
        schemaMappings.put(
            "StatusesRequest",
            "dev.usbharu.hideout.mastodon.interfaces.api.StatusesRequest"
        )
        templateDir.set("$projectDir/templates")
    }
}


sourceSets.main {
    kotlin.srcDirs(
        "$buildDir/generated/sources/mastodon/src/main/kotlin"
    )
}

kover {
    currentProject {
        sources {


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
                title = "Hideout Mastodon"
                xmlFile = file("$buildDir/reports/kover/hideout-mastodon.xml")
            }
        }
        filters {
            excludes {
                annotatedBy("org.springframework.context.annotation.Configuration")
                annotatedBy("org.springframework.boot.context.properties.ConfigurationProperties")
                packages(
                    "dev.usbharu.hideout.controller.mastodon.generated",
                    "dev.usbharu.hideout.domain.mastodon.model.generated"
                )
                packages("org.springframework")
                packages("org.jetbrains")
            }
        }

    }
}
