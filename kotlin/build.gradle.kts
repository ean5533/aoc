plugins {
    id("org.jetbrains.kotlin.jvm") version "1.7.10"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}

application {
    mainClass.set("aoc2022.day1.Day1Kt")
}
