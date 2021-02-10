val libraryVersion = "1.4.3"
val mcVersion = "1.14-R0.1-SNAPSHOT"
val exposedVersionn = "0.29.1"

repositories {
    mavenLocal()
    jcenter()
    mavenCentral()
    maven("https://dl.bintray.com/way-zer/maven")
    maven("https://hub.spigotmc.org/nexus/content/groups/public/")

    maven("https://repo.codemc.org/repository/maven-public")//nbt-api
    maven("https://dl.bintray.com/config4k/config4k")//config4k
}

dependencies {
    val pluginCompile by configurations
    pluginCompile("cf.wayzer:ScriptAgent:$libraryVersion")
    pluginCompile("cf.wayzer:LibraryManager:1.4")
    pluginCompile(kotlin("stdlib-jdk8"))
    pluginCompile("org.spigotmc:spigot-api:$mcVersion")

    val api by configurations
    val implementation by configurations
    api(kotlin("script-runtime"))
    api(kotlin("stdlib-jdk8"))
    api("cf.wayzer:ScriptAgent:$libraryVersion")

    //coreLibrary
    api("cf.wayzer:PlaceHoldLib:3.1")
    api("org.jetbrains.exposed:exposed-core:$exposedVersionn")
    api("org.jetbrains.exposed:exposed-dao:$exposedVersionn")
    api("org.jetbrains.exposed:exposed-java-time:$exposedVersionn")
    api("io.github.config4k:config4k:0.4.1")

    //coreBukkit
    //也可以直接使用服务器正在使用的jar(使用NMS等)
    api("org.spigotmc:spigot-api:$mcVersion")

    //used by superitem
    api("de.tr7zw:item-nbt-api:2.2.0")
    api("org.mapdb:mapdb:3.0.7")
}