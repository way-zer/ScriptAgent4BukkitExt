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

    maven("https://repo.codemc.org/repository/maven-public")//nbt-api
}
sourceSets {
    main {
        java.srcDir("src")
    }
}
dependencies {
    //This is develop snap version,please use release version
    implementation("cf.wayzer:ScriptAgent4Bukkit:1.0-a447f18-DIRTY")
    implementation(kotlin("script-runtime"))
    implementation(kotlin("scripting-common"))//Needed for find @KotlinScript annotation when run init.kts
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.spigotmc:spigot-api:$mc_version")

    //used by superitem
    implementation("de.tr7zw:item-nbt-api:2.2.0")
    implementation("org.mapdb:mapdb:3.0.7")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}