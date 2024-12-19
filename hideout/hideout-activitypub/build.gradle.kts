import kotlinx.kover.gradle.plugin.dsl.CoverageUnit

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kover)
}



tasks.test {
    useJUnitPlatform()
}


tasks {

withType<Test> {
        useJUnitPlatform()
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
                title = "Hideout ActivityPub"
                xmlFile = file("$buildDir/reports/kover/hideout-activitypub.xml")
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
