plugins {
    alias(libs.plugins.kotlin.jvm)
}

group = "dev.usbharu.hideout"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}