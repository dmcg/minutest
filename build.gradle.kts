import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// For Gradle Kotlin DSL help see
// https://github.com/jnizet/gradle-kotlin-dsl-migration-guide

val jvmVersion = "1.8"

repositories {
    mavenCentral()
}

plugins {
    kotlin("jvm") version "1.2.61"
}

apply(plugin = "org.jetbrains.kotlin.jvm")

tasks {
    "compileKotlin"(KotlinJvmCompile::class) {
        kotlinOptions.jvmTarget = jvmVersion
    }

    "compileTestKotlin"(KotlinJvmCompile::class) {
        kotlinOptions.jvmTarget = jvmVersion
    }

    withType<Test> {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("org.junit.jupiter:junit-jupiter-api:5.3.1")
    testRuntime("org.junit.jupiter:junit-jupiter-engine:5.3.1")
}