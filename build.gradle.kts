plugins {
    id("java")
    // ShadowJar (https://github.com/GradleUp/shadow/releases)
    id("com.gradleup.shadow") version "8.3.1"
}

group = "uk.protonull.pistomqueue"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io") // For com.github.MadMartian:hydrazine-path-finding
}

dependencies {
    implementation("net.minestom:minestom-snapshots:+") {
        isChanging = true
        constraints {
            implementation("commons-net:commons-net:3.11.1")
        }
    }
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
        options.release.set(21)
    }
    jar {
        manifest {
            attributes["Main-Class"] = "uk.protonull.pistomqueue.Main"
        }
        from(file("LICENCE")) {
            rename { "LICENSE_PistomQueue" } // Use US spelling
        }
    }
    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        mergeServiceFiles()
        archiveClassifier.set("") // Prevent the -all suffix
    }
}
