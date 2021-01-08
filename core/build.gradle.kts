
import com.jfrog.bintray.gradle.BintrayExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile


repositories {
    mavenCentral()
}

plugins {
    kotlin("jvm")
    maven
    `maven-publish`
    id("com.jfrog.bintray") version "1.8.4"
}

val junitVersion = "5.7.0"

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    implementation("io.github.classgraph:classgraph:4.8.28")

    // alternative runners - provide your own dependencies
    compileOnly("org.junit.vintage:junit-vintage-engine:$junitVersion")
    compileOnly("junit:junit:4.12")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.vintage:junit-vintage-engine:$junitVersion")
    testImplementation("junit:junit:4.12")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("org.junit.platform:junit-platform-launcher:1.7.0") {
        because("enables running in IntelliJ using JUnit runner")
    }

    // for examples
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.3.3")
    testImplementation("com.natpryce:hamkrest:1.7.0.0")
    testImplementation("com.oneeyedmen:k-sera:1.0.0")
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
        testLogging {
            events("skipped", "failed", "passed")
        }
    }

    create<Jar>("sourceJar") {
        archiveClassifier.set("sources")
        from(sourceSets["main"].allSource)
    }

    withType<Jar> {
        archiveBaseName.set("minutest")
    }
}

project.sourceSets {
    val samples = create("samples") {
        java.srcDir(file("src/samples/kotlin"))
        compileClasspath += get("main").output + configurations.testRuntimeClasspath
    }
    get("test").apply {
        compileClasspath += samples.output
        runtimeClasspath += samples.output
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
        name = "minutest.dev"
        version(delegateClosureOf<BintrayExtension.VersionConfig> {
            name = project.version as String
        })
    })
}
