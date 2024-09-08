@file:Suppress("SpellCheckingInspection")

plugins {
    java
    `java-library`
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "me.taromati.chzzklib"
version = "1.0.4"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://libraries.minecraft.net/")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
}

dependencies {
    implementation("org.jsoup:jsoup:1.18.1")
    implementation("org.slf4j:slf4j-api:2.0.7")
    implementation("com.googlecode.json-simple:json-simple:1.1.1")

    compileOnly("net.kyori:adventure-api:4.13.0")
    compileOnly("net.kyori:adventure-text-serializer-legacy:4.13.0")

    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")

    compileOnly("commons-io:commons-io:2.16.1")
    compileOnly("org.jetbrains:annotations:20.1.0")
    compileOnly("com.googlecode.json-simple:json-simple:1.1.1")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.shadowJar {
    archiveFileName.set("ChzzkLib.jar")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "me.taromati"
            artifactId = "chzzklib"

            from(components["java"])
        }
    }

    repositories {
        maven {
            url = uri("https://repo.repsy.io/mvn/lone64/platform")
            credentials {
                username = properties["mavenUser"] as String
                password = properties["mavenPass"] as String
            }
        }
    }
}