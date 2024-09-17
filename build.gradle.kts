plugins {
    id("java")
    id("application")
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
    implementation("net.minestom:minestom-snapshots:4305006e6b") {
        isChanging = true
    }

    implementation("org.tinylog:tinylog-impl:2.7.0")
    implementation("org.tinylog:slf4j-tinylog:2.7.0")

    implementation("com.google.guava:guava:33.3.0-jre")
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass = "uk.protonull.pistomqueue.Main"
    applicationDefaultJvmArgs = listOf(
        "-Xms1G",
        "-Xmx1G",
        "-XX:+UseG1GC",
        "-XX:G1HeapRegionSize=4M",
        "-XX:+UnlockExperimentalVMOptions",
        "-XX:+ParallelRefProcEnabled",
        "-XX:+AlwaysPreTouch",
        "-Dlog4j2.formatMsgNoLookups=true",

        "-Dport=25571",
        "-Dproxy=VELOCITY",
        "-DvelocitySecret=1234567890ABCDEF"
    )
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
        options.release.set(21)
    }
    jar {
        manifest {
            attributes["Main-Class"] = application.mainClass.get()
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
    }
}
