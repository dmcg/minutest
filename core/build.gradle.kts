import com.jfrog.bintray.gradle.BintrayExtension
import org.gradle.api.publish.maven.MavenPom
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


repositories {
    mavenCentral()
}

plugins {
    kotlin("jvm")
    maven
    `maven-publish`
    id("com.jfrog.bintray") version "1.8.1"
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("org.junit.jupiter:junit-jupiter-api:5.3.2")
    implementation("io.github.classgraph:classgraph:4.6.6")

    // alternative runners - provide your own dependencies
    compileOnly("org.junit.vintage:junit-vintage-engine:5.3.2")
    compileOnly("junit:junit:4.12")

    testCompile(kotlin("test"))
    testCompile("org.junit.vintage:junit-vintage-engine:5.3.2")
    testCompile("junit:junit:4.12")
    testCompile("io.github.classgraph:classgraph:4.6.6")
    testCompile("org.junit.jupiter:junit-jupiter-engine:5.3.2")
    testCompile("org.junit.platform:junit-platform-launcher:1.3.2") {
        because("enables running in IntelliJ using JUnit runner")
    }
}

tasks {
    withType<KotlinJvmCompile>() {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }

    withType<Test> {
        useJUnitPlatform {
            includeEngines("junit-jupiter", "junit-vintage")
        }
    }

    create<Jar>("sourceJar") {
        classifier = "sources"
        from(sourceSets["main"].allSource)
    }

    withType<Jar> {
        baseName = "minutest"
    }
}

project.sourceSets {
    create("samples") {
        java.srcDir(file("src/samples/kotlin"))
        compileClasspath += get("main").output + configurations.testRuntimeClasspath
        runtimeClasspath += output + compileClasspath
    }
    get("test").apply {
        compileClasspath += get("samples").output
        runtimeClasspath += get("samples").output
    }
}

artifacts {
    add("archives", tasks["jar"])
    add("archives", tasks["sourceJar"])
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(tasks["sourceJar"])
            groupId = project.group as String
            artifactId = "minutest"
            version = project.version as String
        }
    }
}

// use ./gradlew clean publish bintrayUpload
bintray {
    user = "dmcg"
    key = System.getenv("BINTRAY_API_KEY")
    publish = true
    setPublications("mavenJava")
    pkg(delegateClosureOf<BintrayExtension.PackageConfig> {
        repo = "oneeyedmen-mvn"
        name = "minutest"
        version(delegateClosureOf<BintrayExtension.VersionConfig> {
            name = project.version as String
        })
    })
}
