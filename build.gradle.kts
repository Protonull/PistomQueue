plugins {
    id("java")
    // ShadowJar (https://github.com/johnrengelman/shadow/releases)
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "uk.protonull.pistomqueue"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    // For Minestom
    maven("https://jitpack.io")
}

dependencies {

    implementation("com.github.Minestom:Minestom:-SNAPSHOT") {
        isChanging = true
        constraints {
            implementation("commons-net:commons-net:3.9.0")
        }
    }

    implementation("org.apache.commons:commons-lang3:3.12.0")

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    testCompileOnly(libs.lombok)
    testAnnotationProcessor(libs.lombok)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
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
