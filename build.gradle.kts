plugins {
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("com.github.davidmc24.gradle.plugin.avro") version "1.9.1"; // TODO: End of life - This project is no longer maintained
    id("com.github.ben-manes.versions") version "0.51.0" // Gir oversikt over nyere dependencies med "./gradlew dependencyUpdates"
    application
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("rekrutteringsbistand.stilling.indekser.AppKt")
}

repositories {
    mavenCentral()

    maven {
        url = uri("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
    }

    maven {
        url = uri("https://packages.confluent.io/maven/")
    }
}

dependencies {
    implementation("org.apache.avro:avro:1.12.0")
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("io.javalin:javalin:5.1.1")
    implementation("ch.qos.logback:logback-classic:1.4.4")
    implementation("net.logstash.logback:logstash-logback-encoder:7.2")
    val fuelVersion = "2.3.1"
    implementation("com.github.kittinunf.fuel:fuel:$fuelVersion")
    implementation("com.github.kittinunf.fuel:fuel-jackson:$fuelVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.0")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.1")
    implementation("org.apache.kafka:kafka-clients:3.3.1")
    implementation("io.confluent:kafka-avro-serializer:6.0.1")
    implementation("org.opensearch.client:opensearch-rest-high-level-client:2.3.0")

    testImplementation("io.mockk:mockk:1.13.14")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}
