plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    id("org.jetbrains.kotlin.jvm") version "1.6.21"

    // Apply the java-library plugin for API and implementation separation.
    `java-library`
}

dependencies {
    // logger

    implementation("org.apache.commons:commons-lang3:3.8.1")
    implementation("org.apache.commons:commons-lang3:3.8.1")
    implementation("org.mvel:mvel2:2.4.4.Final")
    implementation("com.jayway.jsonpath:json-path:2.4.0")


    implementation ("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.1")
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    // Use the Kotlin JUnit integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")



}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}
