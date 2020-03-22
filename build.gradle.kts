plugins {
    kotlin("jvm") version "1.3.70"
}

group = "cf.wayzer"
version = "1.0-SNAPSHOT"
val mc_version = "1.14-R0.1-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/groups/public/")
}
sourceSets {
    main {
        java.srcDir("src")
    }
}
dependencies {
    implementation("cf.wayzer:ScriptAgent4Bukkit:1.0-c5444cb")
    implementation(kotlin("script-runtime"))
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.spigotmc:spigot-api:$mc_version")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}