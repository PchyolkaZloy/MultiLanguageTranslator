plugins {
    id("java")
    id("io.spring.dependency-management") version "1.1.4"
    id("org.springframework.boot") version "3.2.5"
}

group = "en.pchz"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.postgresql:postgresql:42.7.3")
    implementation("org.springframework:spring-core:6.1.11")
    implementation("org.springframework:spring-context:6.1.11")
    implementation("org.springframework.boot:spring-boot-starter")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}