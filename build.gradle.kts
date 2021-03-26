

val gradleVersion = "6.8"
val kotlinLoggingVersion = "2.0.6"
val logbackVersion = "1.2.3"
val junitJupiterVersion = "5.7.1"
val kotlinVersion = "1.4.31"
val kotestVersion = "4.4.3"
val nimbusSdkVersion = "8.36"
val mockOAuth2ServerVersion = "0.3.1"
val ktorVersion = "1.5.2"
val org = "tommytroen"

plugins {
    kotlin("jvm") version "1.4.31"
    id("org.jmailen.kotlinter") version "3.3.0"
    id("com.github.ben-manes.versions") version "0.38.0"
    id("se.patrikerdes.use-latest-versions") version "0.2.15"
    `java-library`
    `maven-publish`
}

java {
    sourceCompatibility = JavaVersion.VERSION_14
    targetCompatibility = JavaVersion.VERSION_14
    withJavadocJar()
    withSourcesJar()
}

apply(plugin = "org.jmailen.kotlinter")

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation("io.github.microutils:kotlin-logging:$kotlinLoggingVersion")
    implementation("com.nimbusds:oauth2-oidc-sdk:$nimbusSdkVersion")
    implementation("io.ktor:ktor-auth:$ktorVersion")
    implementation("io.ktor:ktor-auth-jwt:$ktorVersion")
    implementation("io.ktor:ktor-client-jackson:$ktorVersion")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:$kotestVersion") // for kotest framework
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion") // for kotest core jvm assertions
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:$kotlinVersion")
    testImplementation("no.nav.security:mock-oauth2-server:$mockOAuth2ServerVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")
}


publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = rootProject.name
            from(components["java"])

            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
            pom {
                name.set(rootProject.name)
                description.set("A OAuth2 client for any jvm language")
                url.set("https://github.com/$org/${rootProject.name}")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/$org/${rootProject.name}.git")
                    developerConnection.set("scm:git:ssh://github.com/$org/${rootProject.name}.git")
                    url.set("https://github.com/$org/${rootProject.name}")
                }
            }
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/$org/${rootProject.name}")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

tasks.javadoc {
    if (JavaVersion.current().isJava9Compatible) {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.toUpperCase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

tasks.named("dependencyUpdates", com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask::class.java).configure {
    this.
    resolutionStrategy {
        componentSelection {
            all {
                if (isNonStable(candidate.version) ) {
                    reject("Release candidate")
                }
            }
        }
    }
}

tasks.named("useLatestVersions", se.patrikerdes.UseLatestVersionsTask::class.java).configure {
    updateBlacklist = listOf(
        "io.codearte:nexus-staging"
    )
}
tasks {
    withType<org.jmailen.gradle.kotlinter.tasks.LintTask> {
        dependsOn("formatKotlin")
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "14"
        }
    }

    withType<Test> {
        useJUnitPlatform()
    }

    withType<Wrapper> {
        gradleVersion = gradleVersion
    }
}
