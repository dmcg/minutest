
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI

repositories {
    mavenCentral()
}

plugins {
    kotlin("jvm") // NB version is in parent build
    `maven-publish`
    signing
}

val junitVersion = "5.9.0"

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    implementation("io.github.classgraph:classgraph:4.8.28")

    // alternative runners - provide your own dependencies
    compileOnly("org.junit.vintage:junit-vintage-engine:$junitVersion")
    compileOnly("junit:junit:4.13.2")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.vintage:junit-vintage-engine:$junitVersion")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("org.junit.platform:junit-platform-launcher:1.9.0") {
        because("enables running in IntelliJ using JUnit runner")
    }

    // for examples
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.3.3")
    testImplementation("com.natpryce:hamkrest:1.8.0.1")
    testImplementation("com.oneeyedmen:k-sera:1.0.0")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }

    withType<Test> {
        useJUnitPlatform {
            includeEngines("junit-jupiter", "junit-vintage", "minutest")
        }
        testLogging {
            events("skipped", "failed", "passed")
        }
    }

    create<Jar>("sourceJar") {
        archiveClassifier.set("sources")
        from(sourceSets["main"].allSource)
    }

    create<Jar>("javadocJar") {
        archiveClassifier.set("javadoc")
        from(javadoc) // empty at the moment!
    }

    withType<Jar> {
        archiveBaseName.set("minutest")
    }
}

// I can't remember why I did this. The commit says
// Add a samples sourceSet so that we can keep actual tests in src/test
project.sourceSets {
    val samples = create("samples") {
        java.srcDir(file("src/samples/kotlin"))
        compileClasspath += get("main").output + configurations.testRuntimeClasspath.get()
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

signing {
    sign(publishing.publications)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(tasks["sourceJar"])
            artifact(tasks["javadocJar"])
            groupId = project.group as String
            artifactId = "minutest"
            version = project.version as String
            pom {
                name.set("minutest")
                description.set("A testing framework for Kotlin")
                url.set("https://github.com/dmcg/minutest")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("dmcg ")
                        name.set("Duncan McGregor")
                    }
                }
                scm {
                    url.set("https://github.com/dmcg/minutest")
                }
            }
        }
    }
    repositories {
        maven {
            name = "SonatypeStaging"
            url = URI.create("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = "dmcg"
                password = System.getenv("OSS_PWD")
            }
        }
        maven {
            name = "SonatypeSnapshot"
            url = URI.create("https://oss.sonatype.org/content/repositories/snapshots/")
            credentials {
                username = "dmcg"
                password = System.getenv("OSS_PWD")
            }
        }
    }
}


