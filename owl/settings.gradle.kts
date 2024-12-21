plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }

    versionCatalogs {
        create("libs") {
            from(files("../libs.versions.toml"))
        }
    }
}
rootProject.name = "owl"
include("owl-common")
include("owl-producer:owl-producer-api")
findProject(":owl-producer:owl-producer-api")?.name = "owl-producer-api"
include("owl-broker")
include("owl-broker:owl-broker-mongodb")
findProject(":owl-broker:owl-broker-mongodb")?.name = "owl-broker-mongodb"
include("owl-producer:owl-producer-default")
findProject(":owl-producer:owl-producer-default")?.name = "owl-producer-default"
include("owl-consumer")
include("owl-producer:owl-producer-embedded")
include("owl-common:owl-common-serialize-jackson")
findProject(":owl-common:owl-common-serialize-jackson")?.name = "owl-common-serialize-jackson"
