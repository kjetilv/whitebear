plugins {
    kotlin("jvm") version "1.8.22"
    java
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("jakarta.validation:jakarta.validation-api:3.0.1")

    testImplementation(kotlin("test"))
    testImplementation("org.hibernate.validator:hibernate-validator:7.0.2.Final")
    testImplementation("org.glassfish:jakarta.el:4.0.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testImplementation("org.assertj:assertj-core:3.22.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.test {
    useJUnitPlatform()
}
