plugins {
    kotlin("jvm") version "1.3.70"
}

group = "cf.wayzer"
version = "1.0-SNAPSHOT"
val pluginVersion = "1.0.2"
val mcVersion = "1.14-R0.1-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://dl.bintray.com/way-zer/maven")
    maven("https://hub.spigotmc.org/nexus/content/groups/public/")

    maven("https://repo.codemc.org/repository/maven-public")//nbt-api
}
sourceSets {
    main {
        java.srcDir("src")
    }
}
dependencies {
    implementation("cf.wayzer:ScriptAgent4Bukkit:$pluginVersion")
    implementation(kotlin("script-runtime"))
    implementation(kotlin("scripting-common"))//Needed for find @KotlinScript annotation when run init.kts
    implementation(kotlin("stdlib-jdk8"))
    //也可以直接使用服务器正在使用的jar(使用NMS等)
    implementation("org.spigotmc:spigot-api:$mcVersion")

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