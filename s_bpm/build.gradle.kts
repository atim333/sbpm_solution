plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    id("org.jetbrains.kotlin.jvm") version "1.6.21"

    // Apply the java-library plugin for API and implementation separation.
    `java-library`
}

dependencies {
    // logger

    implementation("org.apache.commons:commons-lang3:3.8.1")


}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}
