/*
 * Copyright (C) 2024 usbharu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.github.jk1.license.filter.DependencyFilter
import com.github.jk1.license.filter.LicenseBundleNormalizer
import com.github.jk1.license.importer.DependencyDataImporter
import com.github.jk1.license.importer.XmlReportImporter
import com.github.jk1.license.render.*
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.detekt)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kover)
    alias(libs.plugins.license.report)
}

apply {
    plugin("io.spring.dependency-management")
}


kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
        jvmTarget = JvmTarget.JVM_21
    }
}


repositories {
    mavenCentral()

}


val os = org.gradle.nativeplatform.platform.internal.DefaultNativePlatform.getCurrentOperatingSystem()

dependencies {
    developmentOnly(libs.h2db)
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    detektPlugins(libs.detekt.formatting)

    implementation(libs.bundles.exposed)
    implementation(libs.bundles.coroutines)
    implementation(libs.bundles.ktor.client)
    implementation(libs.bundles.apache.tika)
    implementation(libs.bundles.openapi)
    implementation(libs.bundles.owl.producer)
    implementation(libs.bundles.owl.broker)
    implementation(libs.bundles.spring.boot.oauth2)
    implementation(libs.bundles.spring.boot.data.mongodb)
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    annotationProcessor("org.springframework:spring-context-indexer")

    implementation(libs.blurhash)
    implementation(libs.aws.s3)
    implementation(libs.jsoup)
    implementation(libs.owasp.java.html.sanitizer)
    implementation(libs.postgresql)
    implementation(libs.imageio.webp)
    implementation(libs.thumbnailator)
    implementation(libs.flyway.core)
    runtimeOnly(libs.flyway.postgresql)

    implementation(libs.owl.common.serialize.jackson)

    implementation(libs.javacv) {
        exclude(module = "opencv")
        exclude(module = "flycapture")
        exclude(module = "artoolkitplus")
        exclude(module = "libdc1394")
        exclude(module = "librealsense")
        exclude(module = "librealsense2")
        exclude(module = "tesseract")
        exclude(module = "libfreenect")
        exclude(module = "libfreenect2")
    }
    if (os.isWindows) {
        implementation(variantOf(libs.javacv.ffmpeg) { classifier("windows-x86_64") })
    } else {
        implementation(variantOf(libs.javacv.ffmpeg) { classifier("linux-x86_64") })
    }

    implementation(libs.http.signature)
    implementation(libs.emoji.kt)
    implementation(libs.logback.ecs.encoder)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(libs.kotlin.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.h2db)
    testImplementation(libs.mockito.kotlin)
    testImplementation("org.assertj:assertj-db:3.0.0")
    testImplementation("com.ninja-squad:DbSetup-kotlin:2.1.0")
}

configurations {
    all {
        exclude("org.apache.logging.log4j", "log4j-slf4j2-impl")
    }
}

tasks {
    withType<Test> {
        useJUnitPlatform()
        doFirst {
            jvmArgs = arrayOf(
                "--add-opens",
                "java.base/java.lang=ALL-UNNAMED",
                "--add-opens",
                "java.base/java.util=ALL-UNNAMED",
                "--add-opens",
                "java.naming/javax.naming=ALL-UNNAMED",
                "--add-opens",
                "java.base/java.util.concurrent.locks=ALL-UNNAMED"
            ).toMutableList()
        }
    }
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
                title = "Hideout Core"
                xmlFile = file("$buildDir/reports/kover/hideout-core.xml")
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

licenseReport {
    excludeOwnGroup = true

    importers = arrayOf<DependencyDataImporter>(XmlReportImporter("hideout", File("$projectDir/license-list.xml")))
    renderers = arrayOf<ReportRenderer>(
        InventoryHtmlReportRenderer(), CsvReportRenderer(), JsonReportRenderer(), XmlReportRenderer()
    )
    filters = arrayOf<DependencyFilter>(LicenseBundleNormalizer("$projectDir/license-normalizer-bundle.json", true))
    allowedLicensesFile = File("$projectDir/allowed-licenses.json")
    configurations = arrayOf("productionRuntimeClasspath")
}