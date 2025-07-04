plugins {
    id("java")
}

group = "org.sa"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.0")
    implementation("org.json:json:20240303")
    implementation(project(":console-color"))
}

tasks.test {
    useJUnitPlatform()
}