plugins {
    id("org.jetbrains.kotlin.jvm") version "1.4.21"
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("com.github.davidmc24.gradle.plugin.avro") version "1.2.0"
    application
}

application {
    mainClass.set("rekrutteringsbistand.stilling.indekser.AppKt")
}

repositories {
    maven {
        url = uri("https://jcenter.bintray.com/") // tilsvarer jcenter(), som ble deprecated
    }

    maven {
        url = uri("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
    }

    maven {
        url = uri("https://packages.confluent.io/maven/")
    }
}

dependencies {
    implementation("org.apache.avro:avro:1.10.1")
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
    implementation("io.javalin:javalin:3.12.0")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("net.logstash.logback:logstash-logback-encoder:6.3")
    val fuelVersion = "2.3.0"
    implementation("com.github.kittinunf.fuel:fuel:$fuelVersion")
    implementation("com.github.kittinunf.fuel:fuel-jackson:$fuelVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.0")
    implementation("com.github.ben-manes.caffeine:caffeine:2.8.8")
    implementation("org.apache.kafka:kafka-clients:2.7.0")
    implementation("io.confluent:kafka-avro-serializer:6.0.1")
    implementation("org.elasticsearch.client:elasticsearch-rest-high-level-client:7.10.1")
    implementation("com.github.navikt:rapids-and-rivers:1.389b5c3")

    testImplementation("io.mockk:mockk:1.10.5")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}
