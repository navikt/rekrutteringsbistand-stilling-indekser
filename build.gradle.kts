plugins {
    id("org.jetbrains.kotlin.jvm") version "1.4.21"
    id("com.github.johnrengelman.shadow") version "6.1.0"
    id("com.commercehub.gradle.plugin.avro") version "0.21.0"
    application
}

application {
    mainClassName = "rekrutteringsbistand.stilling.indekser.AppKt"
}

repositories {
    jcenter()

    maven {
        url = uri("https://packages.confluent.io/maven/")
    }
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.javalin:javalin:3.12.0")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("net.logstash.logback:logstash-logback-encoder:6.3")
    implementation("com.github.kittinunf.fuel:fuel:2.3.0")
    implementation("com.github.kittinunf.fuel:fuel-jackson:2.3.0")
    implementation("io.github.cdimascio:dotenv-kotlin:6.2.1")
    implementation("com.github.ben-manes.caffeine:caffeine:2.8.8")
    implementation("org.apache.kafka:kafka-clients:2.7.0")
    implementation("io.confluent:kafka-avro-serializer:6.0.1")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

