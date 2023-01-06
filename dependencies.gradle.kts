repositories {
    mavenLocal()
    if (System.getProperty("user.timezone") == "Asia/Shanghai") {
        maven(url = "https://maven.aliyun.com/repository/public")
    }
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://hub.spigotmc.org/nexus/content/groups/public/")
    maven(url = "https://www.jitpack.io")

    maven("https://repo.codemc.org/repository/maven-public")//nbt-api
    maven("https://maven.tinylake.cf/")//ScriptAgent
}

dependencies {
    val libraryVersion = "1.8.1.2"
    val mcVersion = "1.18-R0.1-SNAPSHOT"
    val exposedVersion = "0.37.3"
    val pluginImplementation by configurations
    pluginImplementation("cf.wayzer:ScriptAgent:$libraryVersion")
    pluginImplementation("cf.wayzer:LibraryManager:1.4.1")
    pluginImplementation("org.spigotmc:spigot-api:$mcVersion")

    val api by configurations
    val implementation by configurations
    api(kotlin("script-runtime"))
    api("cf.wayzer:ScriptAgent:$libraryVersion")

    //coreLibrary
    api("cf.wayzer:PlaceHoldLib:4.3")
    api("io.github.config4k:config4k:0.4.1")
    //coreLib/DBApi
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")

    //coreBukkit
    //也可以直接使用服务器正在使用的jar(使用NMS等)
    api("io.papermc.paper:paper-api:$mcVersion")
    api("net.kyori:adventure-api:4.9.3")

    //used by superitem
    api("de.tr7zw:item-nbt-api:2.8.0")
    api("org.mapdb:mapdb:3.0.7")

    //mirai
    api("net.mamoe:mirai-core-api-jvm:2.8.1")
}