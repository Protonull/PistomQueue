plugins {
    id("java")
    // ShadowJar (https://github.com/johnrengelman/shadow/releases)
    id("com.github.johnrengelman.shadow") version "7.1.2"
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

    implementation("org.apache.commons:commons-lang3:3.12.0")

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    testCompileOnly(libs.lombok)
    testAnnotationProcessor(libs.lombok)
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
    jar {
        manifest {
            attributes["Main-Class"] = "uk.protonull.pistomqueue.Main"
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
