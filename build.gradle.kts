group = "green.sailor"
version = "1.0-SNAPSHOT"

plugins {
    kotlin("jvm").version("1.3.70")
    id("com.diffplug.gradle.spotless").version("3.27.1")
}

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.3")
}

tasks.compileKotlin {
    kotlinOptions.jvmTarget = "11"
}

tasks.compileTestKotlin {
    kotlinOptions.jvmTarget = "11"
}
